package com.kona.common.infrastructure.scheduler

import com.kona.common.infrastructure.util.MDCUtil

abstract class AbstractApplicationScheduler {

    suspend fun <T> executeScheduler(
        lock: suspend (suspend () -> T) -> T,
        block: suspend () -> T
    ): T? {
        return MDCUtil.setMDC {
            lock { block() }
        }
    }

}