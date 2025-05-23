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
    MINUTE_BUCKET(
        note = "분당 트래픽 Bucket Key",
        key = "ktc:{%s}:minute_bucket"
    ),
    MINUTE_BUCKET_REFILL_TIME(
        note = "마지막 분당 Bucket 리필 시간 Key",
        key = "ktc:{%s}:minute_bucket_refill_time"
    ),
    SECOND_BUCKET(
        note = "초당 트래픽 Bucket Key",
        key = "ktc:{%s}:second_bucket"
    ),
    SECOND_BUCKET_REFILL_TIME(
        note = "마지막 초당 Bucket 리필 시간 Key",
        key = "ktc:{%s}:second_bucket_refill_time"
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
                QUEUE                       to QUEUE.getKey(zoneId),
                THRESHOLD                   to THRESHOLD.getKey(zoneId),
                MINUTE_BUCKET               to MINUTE_BUCKET.getKey(zoneId),
                MINUTE_BUCKET_REFILL_TIME   to MINUTE_BUCKET_REFILL_TIME.getKey(zoneId),
                SECOND_BUCKET               to SECOND_BUCKET.getKey(zoneId),
                SECOND_BUCKET_REFILL_TIME   to SECOND_BUCKET_REFILL_TIME.getKey(zoneId),
                ENTRY_COUNT                 to ENTRY_COUNT.getKey(zoneId),
            )
        }

    }

    fun getKey(zoneId: String): String {
        return this.key.format(zoneId)
    }

}