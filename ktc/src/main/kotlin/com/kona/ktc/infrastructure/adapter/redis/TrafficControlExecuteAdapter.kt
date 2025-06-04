package com.kona.ktc.infrastructure.adapter.redis

import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.Companion.getTrafficControlKeys
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.ONE_SECONDS_MILLIS
import com.kona.common.infrastructure.util.ZERO
import com.kona.common.infrastructure.util.calProgressiveEstimatedWaitTime
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import com.kona.ktc.domain.port.outbound.TrafficControlPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Component
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
     *    - 6초당 허용 threshold : threshold / 10
     * 3. windowKey 생성 : "$entryCountKey:$minute:$slot"
     * 4. 대기열 Queue 토큰 추가
     * 5. 현재 slot 진입 카운터 조회
     * 6. 진입 허용 조건 확인: (slot 내 진입 카운터) < allowedPer6Sec && (대기 순번) < allowedPer6Sec
     */
    override suspend fun controlTraffic(traffic: Traffic, now: Instant): TrafficWaiting {
        val zoneId = traffic.zoneId
        val token = traffic.token
        val nowMilli = now.toEpochMilli()

        val trafficControlKeys = getTrafficControlKeys(zoneId)
        val queueKey      = trafficControlKeys[QUEUE]!!
        val thresholdKey  = trafficControlKeys[THRESHOLD]!!
        val entryCountKey = trafficControlKeys[ENTRY_COUNT]!!

        // 1. threshold 조회 (없으면 defaultThreshold 사용)
        val threshold = reactiveStringRedisTemplate.getValue(thresholdKey, defaultThreshold).toLong()

        // 2. 6초 slot 계산
        val minute = nowMilli / ONE_MINUTE_MILLIS
        val secondInMinute = (nowMilli % ONE_MINUTE_MILLIS) / ONE_SECONDS_MILLIS
        val slot = secondInMinute / 6
        val allowedPer6Sec = (threshold / 10L).coerceAtLeast(1L)

        // 3. windowKey 생성
        val windowKey = "$entryCountKey:$minute:$slot"
        println("token: $token, minute: $minute, secondInMinute: $secondInMinute, slot: $slot, allowedPer6Sec: $allowedPer6Sec")

        // 4. 대기열 Queue 토큰 추가
        val exists = reactiveStringRedisTemplate.scoreZSet(queueKey, token)
        if (exists == null) {
            reactiveStringRedisTemplate.addZSet(queueKey, token, nowMilli)
        }

        // 4. 내 순번 확인 (ZRANK 0부터 시작)
        val queuePos = reactiveStringRedisTemplate.rankZSet(queueKey, token)
        if (queuePos == -1L) {
            return TrafficWaiting(-1, -1, -1, -1)
        }

        // 5. 현재 slot 진입 카운터 조회
        val currentCount = reactiveStringRedisTemplate.getValue(windowKey, ZERO.toString()).toLong()
        val queueSize = reactiveStringRedisTemplate.sizeZSet(queueKey)

        // 6. 진입 허용 조건 확인: (slot 내 진입 카운터) < allowedPer6Sec && (대기 순번) < allowedPer6Sec
        return if (currentCount < allowedPer6Sec && queuePos < allowedPer6Sec) {
            reactiveStringRedisTemplate.incrementValue(windowKey)
            reactiveStringRedisTemplate.incrementValue(entryCountKey)
            reactiveStringRedisTemplate.removeZSet(queueKey, token)
            reactiveStringRedisTemplate.expireAndAwait(windowKey, java.time.Duration.ofMinutes(1))
            TrafficWaiting(1, 0, 0, queueSize)
        } else {
            val waitSlot = queuePos / allowedPer6Sec
            val nextSlotStart = nowMilli - (nowMilli % 6000) + (waitSlot * 6000) + 6000
            val waitTime = nextSlotStart - nowMilli
            TrafficWaiting(0, queuePos + 1, waitTime, queueSize, calProgressiveEstimatedWaitTime(queuePos + 1))
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

}
