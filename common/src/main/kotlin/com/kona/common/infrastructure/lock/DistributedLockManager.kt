package com.kona.common.infrastructure.lock

import java.util.concurrent.TimeUnit

interface DistributedLockManager {

    fun <R> lock(
        key: String,
        waitTime: Long = 20,
        leaseTime: Long = 60,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        block: () -> R
    ): R?

}