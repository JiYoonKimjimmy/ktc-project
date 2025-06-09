package com.kona.ktc.infrastructure.adapter.redis

import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.Companion.getTrafficControlKeys
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.QUEUE_ACTIVATION_TIME_KEY
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.ONE_SECONDS_MILLIS
import com.kona.common.infrastructure.util.SIX_SECONDS_MILLIS
import com.kona.common.infrastructure.util.QUEUE_STATUS_KEY
import com.kona.common.infrastructure.util.ZERO
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import com.kona.ktc.domain.port.outbound.TrafficControlPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class TrafficControlExecuteAdapter(
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,
    @Value("\${ktc.traffic.control.defaultThreshold}")
    private val defaultThreshold: String
) : TrafficControlPort {

    /**
     * [트래픽 제어 프로세스]
     * 1. threshold 조회
     * 2. 6초 단위 진입 slot 계산
     *    - 현재시간(분) : nowMilli / 60000
     *    - 현재시간(초) : (nowMilli % 60000) / 1000
     *    - slot : 현재시간(초) / 6
     *    - allowedPer6Sec(6초당 허용 threshold) : threshold / 10
     * 3. windowKey 생성 : "$entryCountKey:$minute:$slot"
     * 4. 대기열 Queue 토큰 추가 (score = 최초 진입 시각)
     * 5. 현재 slot 진입 Count 조회
     * 6. 진입 허용 조건 확인: readyTime 기준
     */
    override suspend fun controlTraffic(traffic: Traffic, now: Instant): TrafficWaiting {
        val zoneId = traffic.zoneId
        val token = traffic.token
        val nowMilli = now.toEpochMilli()

        val trafficControlKeys = getTrafficControlKeys(zoneId)
        val queueKey                = trafficControlKeys[QUEUE]!!
        val queueStatusKey          = trafficControlKeys[QUEUE_STATUS]!!
        val thresholdKey            = trafficControlKeys[THRESHOLD]!!
        val slotWindowKey           = trafficControlKeys[SLOT_WINDOW]!!
        val entryCountKey           = trafficControlKeys[ENTRY_COUNT]!!
        val tokenLastPollingTimeKey = trafficControlKeys[TOKEN_LAST_POLLING_TIME]!!

        val queueStatus = reactiveStringRedisTemplate.getHashValue(queueStatusKey, QUEUE_STATUS_KEY)
        val activationTime = reactiveStringRedisTemplate.getHashValue(queueStatusKey, QUEUE_ACTIVATION_TIME_KEY)?.toLong()

        if (queueStatus == null || (activationTime == null || nowMilli < activationTime)) {
            // queueStatus == null 이거나 activationTime 이 현재 시간보다 큰 경우, 진입 허용 처리
            return TrafficWaiting(1, 0, 0, 0)
        }

        if (queueStatus == TrafficZoneStatus.BLOCKED.name) {
            return TrafficWaiting(-1, 0, 0, 0)
        }

        // 1. threshold 조회 (없으면 defaultThreshold 사용)
        val threshold = reactiveStringRedisTemplate.getValue(thresholdKey, defaultThreshold).toLong()

        // 2. 6초 slot 계산
        val minute = nowMilli / ONE_MINUTE_MILLIS
        val secondInMinute = (nowMilli % ONE_MINUTE_MILLIS) / ONE_SECONDS_MILLIS
        val slot = secondInMinute / 6
        val allowedPer6Sec = (threshold / 10L).coerceAtLeast(1L)

        // 3. windowKey 생성
        val windowKey = "$slotWindowKey:$minute:$slot"

        // 4. 대기열 Queue 토큰 추가 (score = 최초 진입 시각)
        val score = reactiveStringRedisTemplate.scoreZSet(queueKey, token)?.toLong()
        if (score == null) {
            reactiveStringRedisTemplate.addZSet(queueKey, token, nowMilli)
        }
        val entryMilli = score ?: nowMilli

        // 5. 현재 slot 진입 Count 조회
        val currentCount = reactiveStringRedisTemplate.getValue(windowKey, ZERO.toString()).toLong()

        // 6. 진입 허용 조건 확인: readyTime 기준
        val rank = reactiveStringRedisTemplate.rankZSet(queueKey, token)
        val waitSlot = rank / allowedPer6Sec
        val readyTime = entryMilli + (waitSlot * SIX_SECONDS_MILLIS) + SIX_SECONDS_MILLIS

        return if (currentCount < allowedPer6Sec && (rank < allowedPer6Sec || readyTime <= nowMilli)) {
            reactiveStringRedisTemplate.incrementValue(windowKey)
            reactiveStringRedisTemplate.incrementValue(entryCountKey)
            reactiveStringRedisTemplate.removeZSet(queueKey, token)
            reactiveStringRedisTemplate.removeZSet(tokenLastPollingTimeKey, token)
            // windowKey 1분 만료 설정
            reactiveStringRedisTemplate.expireAndAwait(windowKey, Duration.ofMillis(ONE_MINUTE_MILLIS))
            val totalCount = reactiveStringRedisTemplate.sizeZSet(queueKey)
            TrafficWaiting(1, 0, 0, totalCount)
        } else {
            // token request time & count 업데이트
            reactiveStringRedisTemplate.addZSet(tokenLastPollingTimeKey, token, nowMilli)
            // 대기 예상 시간 계산
            val waitTime = maxOf(readyTime - nowMilli, 3 * ONE_SECONDS_MILLIS)
            val totalCount = reactiveStringRedisTemplate.sizeZSet(queueKey)
            TrafficWaiting(0, rank + 1, waitTime, totalCount)
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

}
