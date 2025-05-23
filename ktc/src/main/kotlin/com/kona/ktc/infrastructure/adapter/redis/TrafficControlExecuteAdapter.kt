package com.kona.ktc.infrastructure.adapter.redis

import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.Companion.getTrafficControlKeys
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.ONE_SECONDS_MILLIS
import com.kona.common.infrastructure.util.toTokenScore
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import com.kona.ktc.domain.port.outbound.TrafficControlPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.math.ceil

@Component
class TrafficControlExecuteAdapter(

    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,

    @Value("\${ktc.traffic.control.defaultThreshold}")
    private val defaultThreshold: String

) : TrafficControlPort {

    override suspend fun controlTraffic(traffic: Traffic, now: Instant): TrafficWaiting {
        val zoneId = traffic.zoneId
        val token = traffic.token
        val score = now.toTokenScore()
        val nowMillis = now.toEpochMilli()

        // TrafficCacheKey enum에서 기본 키 가져오기
        val trafficControlKeys = getTrafficControlKeys(zoneId)
        val queueKey            = trafficControlKeys[QUEUE]!!
        val minuteThresholdKey  = trafficControlKeys[THRESHOLD]!!
        val entryCountKey       = trafficControlKeys[ENTRY_COUNT]!!

        // 초당 버킷 관리를 위한 키
        val secondBucketKey           = trafficControlKeys[SECOND_BUCKET]!!
        val secondBucketRefillTimeKey = trafficControlKeys[SECOND_BUCKET_REFILL_TIME]!!

        // 분당 버킷 수 관리를 위한 키
        val minuteBucketKey    = trafficControlKeys[MINUTE_BUCKET]!!
        val minuteLastResetTimeKey = trafficControlKeys[MINUTE_BUCKET_REFILL_TIME]!!

        // 1. 요청 토큰을 대기열(ZSET)에 추가 (이미 있다면 점수 업데이트 안함)
        // rankZSet 결과가 0보다 작으면 해당 멤버가 없다는 의미
        if (reactiveStringRedisTemplate.rankZSet(queueKey, token) < 0) {
            reactiveStringRedisTemplate.addZSet(queueKey, token, score) // 도착 시간(score) 기준으로 정렬
        }

        // 2. 분당 허용량 조회 및 분당 버킷 리필
        val minuteThreshold = reactiveStringRedisTemplate.getValue(minuteThresholdKey, defaultThreshold).toLong()
        val minuteBucketLastRefillTime = reactiveStringRedisTemplate.getValue(minuteLastResetTimeKey, nowMillis.toString()).toLong()
        if (nowMillis - minuteBucketLastRefillTime >= ONE_MINUTE_MILLIS) {
            reactiveStringRedisTemplate.setValue(minuteBucketKey, minuteThreshold.toString())
            reactiveStringRedisTemplate.setValue(minuteLastResetTimeKey, nowMillis.toString())
        }

        // 3. 초당 허용량 조회 및 초당 버킷 리필 (대기열이 있는 경우에만 버킷 리필)
        val totalCountInQueue = reactiveStringRedisTemplate.sizeZSet(queueKey)
        val applySecondBucket = totalCountInQueue >= 2L
        val perSecondThreshold = ceil(minuteThreshold.toDouble() / 60.0).toLong().coerceAtLeast(1)
        if (applySecondBucket) {
            val secondBucketLastRefillTime = reactiveStringRedisTemplate.getValue(secondBucketRefillTimeKey, nowMillis.toString()).toLong()
            if (nowMillis - secondBucketLastRefillTime >= ONE_SECONDS_MILLIS) {
                reactiveStringRedisTemplate.setValue(secondBucketKey, perSecondThreshold.toString())
                reactiveStringRedisTemplate.setValue(secondBucketRefillTimeKey, nowMillis.toString())
            }
        }

        // 4. 요청 진입 가능 여부 판단
        val rank = reactiveStringRedisTemplate.rankZSet(queueKey, token) // 대기열에서 현재 토큰의 순위 (0부터 시작)
        val currentMinuteBucketSize = reactiveStringRedisTemplate.getValue(minuteBucketKey, minuteThreshold.toString()).toLong()
        val currentSecondBucketSize = reactiveStringRedisTemplate.getValue(secondBucketKey, perSecondThreshold.toString()).toLong()

        var canEnter = false // 진입 가능 여부 플래그
        if (currentMinuteBucketSize > 0 && currentSecondBucketSize > 0) {
            if (rank < minuteThreshold) {
                canEnter = true
            } else {
                val tokenScore = reactiveStringRedisTemplate.scoreZSet(queueKey, token).toLong()
                val waitingTime = nowMillis - tokenScore + ONE_SECONDS_MILLIS
                val estimatedTime = ceil((rank + 1).toDouble() / minuteThreshold).toLong() * ONE_MINUTE_MILLIS
                if (waitingTime >= estimatedTime) {
                    canEnter = true
                }
            }
        }

        // 5. 진입 가능/불가능에 따른 처리
        return if (canEnter) {
            // 요청 진입 허용
            reactiveStringRedisTemplate.decrementValue(minuteBucketKey)
            if (applySecondBucket) {
                reactiveStringRedisTemplate.decrementValue(secondBucketKey)
            }
            reactiveStringRedisTemplate.incrementValue(entryCountKey)
            reactiveStringRedisTemplate.removeZSet(queueKey, token)
            TrafficWaiting(true, 0, 0, 0)
        } else {
            val numberInQueue = rank + 1
            val effectiveMinuteThreshold = minuteThreshold.coerceAtLeast(1)
            val estimatedWaitTimeMillis = ceil(numberInQueue.toDouble() / effectiveMinuteThreshold).toLong() * ONE_MINUTE_MILLIS
            TrafficWaiting(false, numberInQueue, estimatedWaitTimeMillis, totalCountInQueue)
        }
    }

    private suspend fun ReactiveStringRedisTemplate.getValue(key: String, default: String? = null): String {
        if (default != null) opsForValue().setIfAbsentAndAwait(key, default)
        return opsForValue().getAndAwait(key)!!
    }

    private suspend fun ReactiveStringRedisTemplate.setValue(key: String, value: String) {
        opsForValue().setAndAwait(key, value)
    }

    private suspend fun ReactiveStringRedisTemplate.incrementValue(key: String, value: Long = 1) {
        opsForValue().incrementAndAwait(key, value)
    }

    private suspend fun ReactiveStringRedisTemplate.decrementValue(key: String, value: Long = 1) {
        opsForValue().decrementAndAwait(key, value)
    }

    private suspend fun ReactiveStringRedisTemplate.rankZSet(key: String, value: String): Long {
        return opsForZSet().rankAndAwait(key, value) ?: -1
    }

    private suspend fun ReactiveStringRedisTemplate.addZSet(key: String, value: String, score: Long) {
        opsForZSet().addAndAwait(key, value, score.toDouble())
    }

    private suspend fun ReactiveStringRedisTemplate.sizeZSet(key: String): Long {
        return opsForZSet().sizeAndAwait(key)
    }

    private suspend fun ReactiveStringRedisTemplate.scoreZSet(key: String, value: String): Double {
        return opsForZSet().scoreAndAwait(key, value)!!
    }

    private suspend fun ReactiveStringRedisTemplate.removeZSet(key: String, value: String) {
        opsForZSet().removeAndAwait(key, value)
    }

}
