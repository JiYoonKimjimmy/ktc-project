package com.kona.ktc.v1.infrastructure.adapter.redis

import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.Companion.getTrafficControlKeys
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.ZERO_STR
import com.kona.common.infrastructure.util.toTokenScore
import com.kona.ktc.v1.domain.model.TrafficToken
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

    override suspend fun controlTraffic(trafficToken: TrafficToken, now: Instant): TrafficWaiting {
        val zoneId = trafficToken.zoneId

        val trafficControlKeys = getTrafficControlKeys(zoneId)
        val queueKey            = trafficControlKeys[QUEUE]!!
        val queueCursorKey      = trafficControlKeys[QUEUE_CURSOR]!!
        val bucketKey           = trafficControlKeys[BUCKET]!!
        val bucketRefillTimeKey = trafficControlKeys[BUCKET_REFILL_TIME]!!
        val thresholdKey        = trafficControlKeys[THRESHOLD]!!

        val score = now.toTokenScore()
        val nowMillis = now.toEpochMilli()

        // 트래픽 요청 토큰 Queue 저장
        if (reactiveStringRedisTemplate.rankZSet(queueKey, trafficToken.token) < 0) {
            reactiveStringRedisTemplate.addZSet(queueKey, trafficToken.token, score)
        }

        // 현재시간 - bucketRefillTime >= 60000ms(1분) 인 경우, cursor & bucket & bucketRefillTime 업데이트
        val bucketRefillTime = reactiveStringRedisTemplate.getValue(bucketRefillTimeKey, nowMillis.toString())
        val threshold = reactiveStringRedisTemplate.getValue(thresholdKey, defaultThreshold).toLong()
        if (nowMillis - bucketRefillTime.toLong() >= ONE_MINUTE_MILLIS) {
            reactiveStringRedisTemplate.incrementValue(queueCursorKey, threshold)
            reactiveStringRedisTemplate.setValue(bucketKey, threshold.toString())
            reactiveStringRedisTemplate.setValue(bucketRefillTimeKey, nowMillis.toString())
        }

        // 트래픽 요청 토큰 rank(순번) 진입 가능 여부 확인
        val rank = reactiveStringRedisTemplate.rankZSet(queueKey, trafficToken.token)
        val queueCursor = reactiveStringRedisTemplate.getValue(queueCursorKey, ZERO_STR).toLong()
        val bucketSize = reactiveStringRedisTemplate.getValue(bucketKey, threshold.toString()).toLong()

        val canEnter = (bucketSize > 0) && (rank in queueCursor until (queueCursor + threshold))
        return if (canEnter) {
            reactiveStringRedisTemplate.decrementValue(bucketKey)
            TrafficWaiting(true, 0, 0, 0)
        } else {
            val queueSize = reactiveStringRedisTemplate.sizeZSet(queueKey)
            val number = rank - queueCursor - threshold - bucketSize + 1
            val estimatedTime = ceil((number.toDouble() / threshold)).toLong() * ONE_MINUTE_MILLIS
            val totalCount = queueSize - queueCursor - threshold - bucketSize
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

}
