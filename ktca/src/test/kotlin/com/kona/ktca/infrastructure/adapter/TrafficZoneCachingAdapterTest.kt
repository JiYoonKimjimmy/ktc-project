package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.testsupport.redis.EmbeddedRedis
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.core.addAndAwait
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.setIfAbsentAndAwait

class TrafficZoneCachingAdapterTest : BehaviorSpec({

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate
    val redisExecuteAdapter = RedisExecuteAdapterImpl(reactiveStringRedisTemplate)
    val trafficZoneCachingAdapter = TrafficZoneCachingAdapter(redisExecuteAdapter)

    given("특정 'zoneId' 전체 Cache 정보 삭제 요청되어") {
        val zoneId = "delete-test-zoneId"
        val keys = TrafficCacheKey.getTrafficControlKeys(zoneId)
        keys.forEach {
            reactiveStringRedisTemplate.opsForValue().setIfAbsentAndAwait(it.value, "test")
        }

        `when`("해당 Zone Cache 정보 삭제 성공인 경우") {
            trafficZoneCachingAdapter.clearAll(listOf(zoneId))

            then("처리 결과 정상 확인한다") {
                val result = keys.map { reactiveStringRedisTemplate.opsForValue().getAndAwait(it.value) }.all { it == null }
                result shouldBe true
            }
        }
    }

    given("모든 Zone 전체 Cache 정보 삭제 요청되어") {
        val zoneIds = listOf("delete-test-zoneId-1", "delete-test-zoneId-2")
        val keys = zoneIds.flatMap { TrafficCacheKey.getTrafficControlKeys(it).values }
        keys.forEach {
            reactiveStringRedisTemplate.opsForValue().setIfAbsentAndAwait(it, "test")
        }

        reactiveStringRedisTemplate.opsForSet().addAndAwait(TrafficCacheKey.ACTIVATION_ZONES.key, *zoneIds.toTypedArray())

        keys.forEach {
            val value = reactiveStringRedisTemplate.opsForValue().getAndAwait(it)
            println("[$it] : $value")
        }

        `when`("모든 Zone Cache 정보 삭제 성공인 경우") {
            trafficZoneCachingAdapter.clearAll(emptyList())

            then("처리 결과 정상 확인한다") {
                val result = keys.map { reactiveStringRedisTemplate.opsForValue().getAndAwait(it) }.all { it == null }
                result shouldBe true
            }
        }
    }

})