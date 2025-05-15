package com.kona.common.infrastructure.lock

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.util.DATE_TIME_PATTERN_yyyyMMddHHmm
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.common.testsupport.redis.EmbeddedRedisTestListener
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.time.LocalDateTime
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

    "'expireTrafficTokenScheduleLock' 중복 Lock 점유 요청 예외 발생 정상 확인한다" {
        // given
        val dateTime = LocalDateTime.now().convertPatternOf(DATE_TIME_PATTERN_yyyyMMddHHmm)
        val block: () -> String = {
            Thread.sleep(500)
            "Hello World, $dateTime."
        }

        // when
        val task1 = async(Dispatchers.Default) { distributedLockManager.expireTrafficTokenScheduleLock(dateTime) { block() } }
        val task2 = async(Dispatchers.Default) {
            delay(100)
            shouldThrow<InternalServiceException> { distributedLockManager.expireTrafficTokenScheduleLock(dateTime) { block() } }
        }

        // then
        task1.await() shouldBe "Hello World, $dateTime."
        task2.await().errorCode shouldBe ErrorCode.REDISSON_LOCK_ATTEMPT_ERROR
    }

})