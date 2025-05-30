package com.kona.ktc.infrastructure.adapter.redis

import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.Companion.getTrafficControlKeys
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.util.SIX_SECONDS_MILLIS
import com.kona.common.infrastructure.util.ZERO
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import com.kona.ktc.domain.port.outbound.TrafficControlPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.math.abs
import kotlin.math.ceil

@Component
class TrafficControlExecuteAdapter(
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,
    @Value("\${ktc.traffic.control.defaultThreshold}")
    private val defaultThreshold: String
) : TrafficControlPort {

    /**
     * [트래픽 제어 프로세스]
     * 1. 트래픽 Zone 상태 확인
     *    - `status == BLOCKED` 인 경우, `result: -1` 차단 응답 처리
     * 2. 트래픽 대기 요청 Token Queue 저장
     * 3. Bucket Refill 여부 확인
     *    - `nowMillis - bucketRefillTime >= 1000ms(1초)` 인 경우, `bucket` 리필 & `bucketRefillTime` 업데이트
     * 4. 트래픽 진입 가능 여부 확인
     *    - 진입 가능 여부 : `abs(rank - (entryCount % (threshold / 10)) / threshold == 0`
     *    4.1. 진입 가능한 경우, `result: 1` 진입 허용 응답 처리
     *         - `bucket` 차감 처리
     *         - `entryCount` 증가 처리
     *         - `queue` token 삭제 처리
     *    4.2. 진입 불가한 경우, `result: 0` 진입 불가 응답 처리
     *         - `number`        : `rank + 1`
     *         - `estimatedTime` : `(ceil(rank / (threshold / 10) + 1) * 6000`(ms)
     *         - `totalCount`    : `queue size`
     */
    override suspend fun controlTraffic(traffic: Traffic, now: Instant): TrafficWaiting {
        val zoneId = traffic.zoneId
        val token = traffic.token
        val nowMillis = now.toEpochMilli()

        // 트래픽 제어 관련 Cache Key
        val trafficControlKeys = getTrafficControlKeys(zoneId)
        val queueKey                  = trafficControlKeys[QUEUE]!!
        val queueStatusKey            = trafficControlKeys[QUEUE_STATUS]!!
        val thresholdKey              = trafficControlKeys[THRESHOLD]!!
        val secondBucketKey           = trafficControlKeys[SECOND_BUCKET]!!
        val secondBucketRefillTimeKey = trafficControlKeys[SECOND_BUCKET_REFILL_TIME]!!
        val entryCountKey             = trafficControlKeys[ENTRY_COUNT]!!
        val tokenLastEntryTimeKey     = trafficControlKeys[TOKEN_LAST_ENTRY_TIME]!!

        // 1. 트래픽 Zone 상태 확인
        val queueStatus = reactiveStringRedisTemplate.getValue(queueStatusKey, TrafficZoneStatus.ACTIVE.name)
        if (queueStatus == TrafficZoneStatus.BLOCKED.name) throw InternalServiceException(ErrorCode.TRAFFIC_ZONE_STATUS_IS_BLOCKED)

        // 2. 트래픽 대기 요청 Token Queue 저장
        if (reactiveStringRedisTemplate.rankZSet(queueKey, token) < 0) {
            reactiveStringRedisTemplate.addZSet(queueKey, token, nowMillis)
        }

        // 3. Bucket Refill 여부 확인
        //    - `nowMillis - bucketRefillTime >= 1000ms(1초)` 인 경우, `bucket` 리필 & `bucketRefillTime` 업데이트
        val threshold = reactiveStringRedisTemplate.getValue(thresholdKey, defaultThreshold).toLong()
        val secondThreshold = ceil(threshold / 10.0).toLong()
        val bucketRefillTime = reactiveStringRedisTemplate.getValue(secondBucketRefillTimeKey, nowMillis.toString()).toLong()
//        println("token: $token, secondThreshold: $secondThreshold, now: $nowMillis, bucketRefillTime: $bucketRefillTime, now - bucketRefillTime: ${nowMillis - bucketRefillTime}")
        if (nowMillis - bucketRefillTime >= 6000) {
            reactiveStringRedisTemplate.setValue(secondBucketKey, secondThreshold.toString())
            reactiveStringRedisTemplate.setValue(secondBucketRefillTimeKey, nowMillis.toString())
        }

        // 4. 트래픽 진입 가능 여부 확인
        //    - 진입 가능 여부 : 초당 첫번째 그룹 && Bucket 진입 가능한 상태인 경우
        //    - 진입 가능 여부 : `abs(rank - (entryCount % (threshold / 10))) / threshold == 0` and `bucket > 0`
        //    4.1. 진입 가능한 경우, `result: 1` 진입 허용 응답 처리
        //         - `bucket` 차감 처리
        //         - `entryCount` 증가 처리
        //         - `queue` token 삭제 처리
        //    4.2. 진입 불가한 경우, `result: 0` 진입 불가 응답 처리
        //         - `number`        : `rank + 1`
        //         - `estimatedTime` : `(ceil(rank / (threshold / 10) + 1) * 6000`(ms)
        //         - `totalCount`    : `queue size`
        val rank = reactiveStringRedisTemplate.rankZSet(queueKey, token)
        val entryCount = reactiveStringRedisTemplate.getValue(entryCountKey, ZERO.toString()).toLong()
        val bucketSize = reactiveStringRedisTemplate.getValue(secondBucketKey, secondThreshold.toString()).toLong()
        val canEnter = abs(rank - (entryCount % ceil(threshold / 10.0))).toLong() / threshold == ZERO && bucketSize > ZERO

        return if (canEnter) {
            reactiveStringRedisTemplate.decrementValue(secondBucketKey)
            reactiveStringRedisTemplate.incrementValue(entryCountKey)
            reactiveStringRedisTemplate.removeZSet(queueKey, token)
//            println("token: $token, number: ${rank + 1}, threshold: $threshold, secondThreshold: $secondThreshold, estimatedTime: 0ms")
            TrafficWaiting(1, 0, 0, 0)
        } else {
            val number = rank + 1
            val totalCount = reactiveStringRedisTemplate.sizeZSet(queueKey)
            val tokenLastEntryTime = reactiveStringRedisTemplate.getHashValue(tokenLastEntryTimeKey, token, nowMillis)
            val marginTime = nowMillis - tokenLastEntryTime
            val estimatedTime = (ceil(number / secondThreshold.toDouble()) * SIX_SECONDS_MILLIS).toLong() - marginTime
//            println("token: $token, number: $number, threshold: $threshold, secondThreshold: $secondThreshold, estimatedTime: ${estimatedTime}ms(${formatMillisToMinSec(estimatedTime)}), marginTime: ${marginTime}ms")

            // 트래픽 대기 요청 Token 마지막 진입 요청 시간 저장
            reactiveStringRedisTemplate.putHashValue(tokenLastEntryTimeKey, token, nowMillis)

            TrafficWaiting(0, number, estimatedTime, totalCount)
        }
    }

    private fun formatMillisToMinSec(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "${minutes}분 ${seconds}초"
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

    private suspend fun ReactiveStringRedisTemplate.putHashValue(key: String, hashKey: String, hashValue: Long) {
        opsForHash<String, String>().putAndAwait(key, hashKey, hashValue.toString())
    }

    private suspend fun ReactiveStringRedisTemplate.getHashValue(key: String, hashKey: String, default: Long = 0): Long {
        return opsForHash<String, String>().getAndAwait(key, hashKey)?.toLong() ?: default
    }

}
