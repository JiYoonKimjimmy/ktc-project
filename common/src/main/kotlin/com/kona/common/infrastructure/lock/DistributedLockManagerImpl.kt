package com.kona.common.infrastructure.lock

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class DistributedLockManagerImpl(
    private val redissonClient: RedissonClient
) : DistributedLockManager {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun <R> lock(
        key: String,
        waitTime: Long,
        leaseTime: Long,
        timeUnit: TimeUnit,
        block: () -> R
    ): R {
        val lock = redissonClient.getLock(key)

        return try {
            if (lock.tryLock(waitTime, leaseTime, timeUnit)) {
                logger.info("Redisson '$key' locked.")
                block()
            } else {
                logger.info("Redisson '$key' lock attempt failed.")
                throw InternalServiceException(ErrorCode.REDISSON_LOCK_ATTEMPT_ERROR)
            }
        } finally {
            try {
                if (lock.isHeldByCurrentThread) {
                    lock.unlock().also { logger.info("Redisson '$key' unlocked.") }
                }
            } catch (e: IllegalMonitorStateException) {
                logger.error("Redisson '$key' Lock already unLock.")
            }
        }
    }

}