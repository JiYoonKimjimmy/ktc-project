package com.kona.ktc.v1.infrastructure.adapter.redis

import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.Companion.getTrafficControlKeys
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.ONE_SECONDS_MILLIS
import com.kona.common.infrastructure.util.toTokenScore
import com.kona.ktc.v1.domain.model.Traffic
import com.kona.ktc.v1.domain.model.TrafficWaiting
import com.kona.ktc.v1.domain.port.outbound.TrafficControlPort
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

        val trafficControlKeys = getTrafficControlKeys(zoneId)
        val queueKey            = trafficControlKeys[QUEUE]!!
        val thresholdKey        = trafficControlKeys[THRESHOLD]!!
        val bucketKey           = trafficControlKeys[BUCKET]!!
        val bucketRefillTimeKey = trafficControlKeys[BUCKET_REFILL_TIME]!!
        val entryCountKey       = trafficControlKeys[ENTRY_COUNT]!!

        // 트래픽 대기 token Queue 저장
        if (reactiveStringRedisTemplate.rankZSet(queueKey, token) < 0) {
            reactiveStringRedisTemplate.addZSet(queueKey, token, score)
        }

        // bucket refill 여부 확인 : nowMills - bucketRefillTime >= 60000ms 인 경우, bucket 리필 처리
        val threshold = reactiveStringRedisTemplate.getValue(thresholdKey, defaultThreshold).toLong()
        val bucketRefillTime = reactiveStringRedisTemplate.getValue(bucketRefillTimeKey, nowMillis.toString()).toLong()
        if (nowMillis - bucketRefillTime >= ONE_MINUTE_MILLIS) {
            reactiveStringRedisTemplate.setValue(bucketKey, threshold.toString())
            reactiveStringRedisTemplate.setValue(bucketRefillTimeKey, nowMillis.toString())
        }

        // 트래픽 진입 가능 여부 확인 : rank < threshold && bucketSize > 0
        val rank = reactiveStringRedisTemplate.rankZSet(queueKey, token)
        val bucketSize = reactiveStringRedisTemplate.getValue(bucketKey, threshold.toString()).toLong()
        var canEnter = false

        if (rank < threshold && bucketSize > 0) {
            canEnter = true
        } else {
            val tokenScore = reactiveStringRedisTemplate.scoreZSet(queueKey, token).toLong()
            val waitingTime = nowMillis - tokenScore + ONE_SECONDS_MILLIS
            val estimatedTime = ceil((rank + 1).toDouble() / threshold).toLong() * ONE_MINUTE_MILLIS
            if (waitingTime >= estimatedTime && bucketSize > 0) {
                canEnter = true
            }
        }

        return if (canEnter) {
            reactiveStringRedisTemplate.decrementValue(bucketKey)
            reactiveStringRedisTemplate.incrementValue(entryCountKey)
            reactiveStringRedisTemplate.removeZSet(queueKey, token)
            TrafficWaiting(true, 0, 0, 0)
        } else {
            // 트래픽 대기 정보 응답 처리
            val number = rank + 1
            val estimatedTime = ceil((number.toDouble() / threshold)).toLong() * ONE_MINUTE_MILLIS
            val totalCount = reactiveStringRedisTemplate.sizeZSet(queueKey)
            TrafficWaiting(false, number, estimatedTime, totalCount)
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
