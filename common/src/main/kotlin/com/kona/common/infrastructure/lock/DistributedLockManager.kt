package com.kona.common.infrastructure.lock

import com.kona.common.infrastructure.util.DATE_TIME_BASIC_PATTERN
import com.kona.common.infrastructure.util.convertPatternOf
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

interface DistributedLockManager {

    suspend fun <R> lock(
        key: String,
        waitTime: Long = 20,
        leaseTime: Long = 60,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        block: suspend () -> R
    ): R?

    suspend fun <R> expireTrafficTokenSchedulerLock(
        now: String = LocalDateTime.now().convertPatternOf(DATE_TIME_BASIC_PATTERN),
        block: suspend () -> R
    ): R

    suspend fun <R> collectTrafficZoneMonitoringSchedulerLock(
        now: String = LocalDateTime.now().convertPatternOf(DATE_TIME_BASIC_PATTERN),
        block: suspend () -> R
    ): R

}