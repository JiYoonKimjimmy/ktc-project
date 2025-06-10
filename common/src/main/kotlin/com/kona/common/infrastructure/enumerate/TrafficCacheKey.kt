package com.kona.common.infrastructure.enumerate

enum class TrafficCacheKey(
    private val note: String,
    val key: String
) {

    QUEUE(
        note = "트래픽 대기 Queue Key",
        key = "ktc:{%s}:queue"
    ),
    QUEUE_STATUS(
        note = "트래픽 대기 Queue 상태 Key",
        key = "ktc:{%s}:queue_status"
    ),
    THRESHOLD(
        note = "트래픽 분당 임계치 Key",
        key = "ktc:{%s}:threshold"
    ),
    SLOT_WINDOW(
        note = "트래픽 진입 Slow Window Key",
        key = "ktc:{%s}:slot:window"
    ),
    ENTRY_COUNT(
        note = "트래픽 진입 Count Key",
        key = "ktc:{%s}:entry_count"
    ),
    TOKEN_LAST_POLLING_TIME(
        note = "트래픽 Token 마지막 Polling 시간 Key",
        key = "ktc:{%s}:token_last_polling_time"
    ),
    ACTIVATION_ZONES(
        note = "트래픽 제어 활성화 Zone 목록 Key",
        key = "ktc:activation:zones"
    );

    companion object {

        fun generate(zoneId: String): Map<TrafficCacheKey, String> {
            return entries.associateWith { it.key.format(zoneId) }
        }

        fun getTrafficControlKeys(zoneId: String): Map<TrafficCacheKey, String> {
            return mapOf(
                QUEUE                   to QUEUE.getKey(zoneId),
                QUEUE_STATUS            to QUEUE_STATUS.getKey(zoneId),
                THRESHOLD               to THRESHOLD.getKey(zoneId),
                SLOT_WINDOW             to SLOT_WINDOW.getKey(zoneId),
                ENTRY_COUNT             to ENTRY_COUNT.getKey(zoneId),
                TOKEN_LAST_POLLING_TIME to TOKEN_LAST_POLLING_TIME.getKey(zoneId)
            )
        }

    }

    fun getKey(zoneId: String): String {
        return this.key.format(zoneId)
    }

}