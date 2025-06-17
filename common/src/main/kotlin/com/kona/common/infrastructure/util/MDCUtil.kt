package com.kona.common.infrastructure.util

import org.slf4j.MDC

object MDCUtil {

    const val CORRELATION_ID_LOG_FIELD = "correlationId"

    inline fun <T> setMDC(block: () -> T): T {
        try {
            MDC.put(CORRELATION_ID_LOG_FIELD, getCorrelationId())
            return block()
        } finally {
            MDC.remove(CORRELATION_ID_LOG_FIELD)
        }
    }

}