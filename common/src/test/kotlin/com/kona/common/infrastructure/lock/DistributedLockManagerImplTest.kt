package com.kona.common.infrastructure.lock

import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.common.testsupport.redis.EmbeddedRedisTestListener
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.TimeUnit

class DistributedLockManagerImplTest : StringSpec({

    listeners(EmbeddedRedisTestListener())

    val redissonClient = EmbeddedRedis.redissonClient
    val distributedLockManager = DistributedLockManagerImpl(redissonClient)

    "RedissonClient 활용한 Distributed Lock 처리 정상 확인한다" {
        // given
        val timeUnit = TimeUnit.SECONDS
        val waitTime = 5L
        val leaseTime = 10L
        val lockKey = "redissonClient-test-lock"
        val lock = redissonClient.getLock(lockKey)

        // when then
        try {
            val result = lock.tryLock(waitTime, leaseTime, timeUnit)

            result shouldBe true

            println("!! $lockKey Locked !!")
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
                println("!! $lockKey Released !!")
            }
        }
    }

    "DistributedLockManager 활용한 Distributed Lock 처리 정상 확인한다" {
        // given
        val timeUnit = TimeUnit.SECONDS
        val waitTime = 5L
        val leaseTime = 10L
        val lockKey = "distributedLockManager-test-lock"

        // when
        val result = distributedLockManager.lock(lockKey, waitTime, leaseTime, timeUnit) {
            "Hello World!!"
        }

        // then
        result shouldBe "Hello World!!"
    }

})