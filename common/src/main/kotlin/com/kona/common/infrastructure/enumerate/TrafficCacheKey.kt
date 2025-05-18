package com.kona.common.infrastructure.enumerate

enum class TrafficCacheKey(
    private val note: String,
    val key: String
) {

    QUEUE(
        note = "트래픽 대기 Queue Key",
        key = "ktc:{%s}:queue"
    ),
    QUEUE_CURSOR(
        note = "트래픽 대기 Queue Cursor Key",
        key = "ktc:{%s}:queue_cursor"
    ),
    BUCKET(
        note = "트래픽 Bucket Key",
        key = "ktc:{%s}:bucket"
    ),
    BUCKET_REFILL_TIME(
        note = "마지막 Bucket 리필 시간 Key",
        key = "ktc:{%s}:bucket_refill_time"
    ),
    THRESHOLD(
        note = "트래픽 분당 임계치 Key",
        key = "ktc:{%s}:threshold"
    ),
    ENTRY_COUNT(
        note = "트래픽 진입 Count Key",
        key = "ktc:{%s}:entry_count"
    ),
    ACTIVATION_ZONES(
        note = "트래픽 제어 활성화 Zone 목록 Key",
        key = "ktc:activation:zones"
    )
    ;

    companion object {

        fun generate(zoneId: String): Map<TrafficCacheKey, String> {
            return entries.associateWith { it.key.format(zoneId) }
        }

        fun getTrafficControlKeys(zoneId: String): Map<TrafficCacheKey, String> {
            return mapOf(
                QUEUE               to QUEUE.getKey(zoneId),
                THRESHOLD           to THRESHOLD.getKey(zoneId),
                BUCKET              to BUCKET.getKey(zoneId),
                BUCKET_REFILL_TIME  to BUCKET_REFILL_TIME.getKey(zoneId),
                ENTRY_COUNT         to ENTRY_COUNT.getKey(zoneId),
            )
        }

    }

    fun getKey(zoneId: String): String {
        return this.key.format(zoneId)
    }

}