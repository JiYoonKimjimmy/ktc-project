package com.kona.ktc.v1.domain.model

data class TrafficWaiting(
    val number: Long,
    val estimatedTime: Long,
    val totalCount: Long,
    val pollingPeriod: Long = 3000L
) {
    val canEnter: Boolean by lazy { this.estimatedTime == 0L }

    companion object {
        fun entry(): TrafficWaiting {
            return TrafficWaiting(0, 0, 0)
        }

        fun waiting(number: Long, estimatedTime: Long, totalCount: Long): TrafficWaiting {
            return TrafficWaiting(number, estimatedTime, totalCount)
        }
    }
}