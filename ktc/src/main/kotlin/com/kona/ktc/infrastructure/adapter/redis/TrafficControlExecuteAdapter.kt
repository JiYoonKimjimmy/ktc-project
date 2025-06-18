package com.kona.ktc.infrastructure.adapter.redis

import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.Companion.getTrafficControlKeys
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.*
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import com.kona.ktc.domain.port.outbound.TrafficControlPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import kotlin.math.max

@Component
class TrafficControlExecuteAdapter(
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,
    @Value("\${ktc.traffic.control.defaultThreshold}")
    private val defaultThreshold: String
) : TrafficControlPort {

    private suspend fun logging(msg: String? = null) {
        val isLogging = false
        if (isLogging) {
            msg?.let { print(it) } ?: println()
        }
    }

    /**
     * [트래픽 제어 프로세스]
     * 1. threshold 조회
     * 2. 6초 단위 진입 slot 계산
     *    - 현재시간(분) : nowMilli / 60000
     *    - 현재시간(초) : (nowMilli % 60000) / 1000
     *    - slot : 현재시간(초) / 6
     *    - allowedPer6Sec(6초당 허용 threshold) : threshold / 10
     * 3. window & slot count 캐시 조회
     * 4. 대기열 Queue 토큰 추가 (score = 최초 진입 시각)
     * 5. 진입 허용 조건 확인
     *    - 현재 Bucket Count 충분한 경우, 무조건 진입
     *    - 현재 slotEntryCount < allowedPer6Sec (6초당 허용 threshold)
     *    - 현재 rank(대기 순번) < allowedPer6Sec (6초당 허용 threshold) && waitingTime(token 대기 시간) <= 현재시간(초)
     */
    override suspend fun controlTraffic(traffic: Traffic, now: Instant): TrafficWaiting {
        val zoneId = traffic.zoneId
        val token = traffic.token
        val nowMilli = now.toEpochMilli()

        val trafficControlKeys = getTrafficControlKeys(zoneId)
        val queueKey                = trafficControlKeys[QUEUE]!!
        val queueStatusKey          = trafficControlKeys[QUEUE_STATUS]!!
        val thresholdKey            = trafficControlKeys[THRESHOLD]!!
        val entryWindowKey          = trafficControlKeys[ENTRY_WINDOW]!!
        val entrySlotKey            = trafficControlKeys[ENTRY_SLOT]!!
        val entryCountKey           = trafficControlKeys[ENTRY_COUNT]!!
        val tokenLastPollingTimeKey = trafficControlKeys[TOKEN_LAST_POLLING_TIME]!!

        val queueStatus = reactiveStringRedisTemplate.getHashValue(queueStatusKey, QUEUE_STATUS_KEY)
        val activationTime = reactiveStringRedisTemplate.getHashValue(queueStatusKey, QUEUE_ACTIVATION_TIME_KEY)?.toLong()

        if (queueStatus == null) {
            // Queue status 정보 없는 경우, 차단('TRAFFIC_ZONE_NOT_FOUND') 처리
            return TrafficWaiting(-100, 0, 0, 0)
        } else if (queueStatus == TrafficZoneStatus.BLOCKED.name) {
            // Queue status 'BLOCKED' 인 경우, 차단('TRAFFIC_ZONE_BLOCKED') 처리
            return TrafficWaiting(-102, 0, 0, 0)
        } else if (queueStatus == TrafficZoneStatus.FAULTY_503.name) {
            // Queue status 'FAULTY_503' 인 경우, 진입 장애 차단('FAULTY_503_ERROR') 처리
            return TrafficWaiting(-503, 0, 0, 0)
        } else if (activationTime != null && nowMilli < activationTime) {
            // Queue activation 시간이 현재보다 이전인 경우, 진입 처리
            return TrafficWaiting(1, 0, 0, 0)
        }

        // 1. threshold 조회 (없으면 defaultThreshold 사용)
        val threshold = reactiveStringRedisTemplate.getValue(thresholdKey, defaultThreshold).toLong()

        // 2. 6초 slot 계산
        val minute = nowMilli / ONE_MINUTE_MILLIS
        val secondInMinute = (nowMilli % ONE_MINUTE_MILLIS) / ONE_SECONDS_MILLIS
        val slot = secondInMinute / 6
        val allowedPer6Sec = (threshold / 10L).coerceAtLeast(1L)

        // 3. windowKey & slotCountKey 생성
        val windowCountKey = "$entryWindowKey:$minute"
        val slotCountKey = "$entrySlotKey:$minute:$slot"
        val windowEntryCount = reactiveStringRedisTemplate.getValue(windowCountKey, ZERO.toString()).toLong()
        val slotEntryCount = reactiveStringRedisTemplate.getValue(slotCountKey, ZERO.toString()).toLong()
        val queueSize = reactiveStringRedisTemplate.sizeZSet(queueKey)

        // 4. 대기열 Queue 토큰 추가 (score = 최초 진입 시각)
        val score = reactiveStringRedisTemplate.scoreZSet(queueKey, token)?.toLong()
        if (score == null) {
            reactiveStringRedisTemplate.addZSet(queueKey, token, nowMilli)
        }
        val entryMilli = score ?: nowMilli

        // 5. 진입 허용 조건 확인
        val rank = reactiveStringRedisTemplate.rankZSet(queueKey, token)
        val waitSlot = rank / allowedPer6Sec
        val entryAvailableTime = entryMilli + (waitSlot * SIX_SECONDS_MILLIS) + SIX_SECONDS_MILLIS

        val canEnter = if (windowEntryCount < threshold && queueSize == 0L) {
            true
        } else if (slotEntryCount < allowedPer6Sec && (rank < allowedPer6Sec || entryAvailableTime <= nowMilli)) {
            true
        } else {
            false
        }

        logging("token: $token")
        logging(", canEnter: $canEnter")

        return if (canEnter) {
            reactiveStringRedisTemplate.incrementValue(windowCountKey)
            reactiveStringRedisTemplate.incrementValue(slotCountKey)
            reactiveStringRedisTemplate.incrementValue(entryCountKey)
            reactiveStringRedisTemplate.removeZSet(queueKey, token)
            reactiveStringRedisTemplate.removeZSet(tokenLastPollingTimeKey, token)
            // CountKey 1분 만료 설정
            reactiveStringRedisTemplate.expireAndAwait(windowCountKey, Duration.ofMillis(ONE_MINUTE_MILLIS))
            reactiveStringRedisTemplate.expireAndAwait(slotCountKey, Duration.ofMillis(ONE_MINUTE_MILLIS))
            val totalCount = reactiveStringRedisTemplate.sizeZSet(queueKey)
            logging()
            TrafficWaiting(1, 0, 0, totalCount)
        } else {
            // token request time & count 업데이트
            // 대기 예상 시간 계산
            logging(", rank: $rank")
            logging(", waitSlot: $waitSlot")

            val tokenLastPollingTime = reactiveStringRedisTemplate.scoreZSet(tokenLastPollingTimeKey, token)?.toLong() ?: nowMilli
            logging(", tokenLastPollingTime: $tokenLastPollingTime")
            logging(", entryMilli: $entryMilli")

            val estimatedTime = max(calculateTokenWaitingTime(waitSlot, tokenLastPollingTime, entryMilli, nowMilli), 3 * ONE_SECONDS_MILLIS)
            logging(", estimatedTime: ${estimatedTime}ms (${printETA(estimatedTime)})")
            logging()
            val totalCount = reactiveStringRedisTemplate.sizeZSet(queueKey)
            reactiveStringRedisTemplate.addZSet(tokenLastPollingTimeKey, token, nowMilli)
            TrafficWaiting(0, rank + 1, estimatedTime, totalCount)
        }
    }

    private suspend fun calculateTokenWaitingTime(waitSlot: Long, tokenLastPollingTime: Long, entryMilli: Long, nowMilli: Long): Long {
        val estimatedTime = (waitSlot + 1) * SIX_SECONDS_MILLIS
        val totalWaitingTime =  if (tokenLastPollingTime == entryMilli) {
            nowMilli - entryMilli
        } else {
            tokenLastPollingTime - entryMilli
        }
        val result = estimatedTime - totalWaitingTime
        return if (result <= 0) {
            3 * ONE_SECONDS_MILLIS
        } else if (estimatedTime < totalWaitingTime) {
            estimatedTime
        } else {
            result
        }
    }

    private suspend fun ReactiveStringRedisTemplate.getValue(key: String, default: String? = null): String {
        if (default != null) opsForValue().setIfAbsentAndAwait(key, default)
        return opsForValue().getAndAwait(key)!!
    }

    private suspend fun ReactiveStringRedisTemplate.incrementValue(key: String, value: Long = 1) {
        opsForValue().incrementAndAwait(key, value)
    }

    private suspend fun ReactiveStringRedisTemplate.rankZSet(key: String, value: String): Long {
        return opsForZSet().rankAndAwait(key, value) ?: -1
    }

    private suspend fun ReactiveStringRedisTemplate.addZSet(key: String, value: String, score: Long) {
        opsForZSet().addAndAwait(key, value, score.toDouble())
    }

    private suspend fun ReactiveStringRedisTemplate.scoreZSet(key: String, value: String): Double? {
        return opsForZSet().scoreAndAwait(key, value)
    }

    private suspend fun ReactiveStringRedisTemplate.removeZSet(key: String, value: String) {
        opsForZSet().removeAndAwait(key, value)
    }

    private suspend fun ReactiveStringRedisTemplate.sizeZSet(key: String): Long {
        return opsForZSet().sizeAndAwait(key)
    }

    private suspend fun ReactiveStringRedisTemplate.getHashValue(key: String, hashKey: String): String? {
        return opsForHash<String, String>().getAndAwait(key, hashKey)
    }

    private suspend fun printETA(eta: Long): String {
        val totalSeconds = eta / 1000
        return "${totalSeconds / 60}분 ${totalSeconds % 60}초"
    }

}
