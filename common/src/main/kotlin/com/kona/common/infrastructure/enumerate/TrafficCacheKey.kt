package com.kona.common.infrastructure.enumerate

enum class TrafficCacheKey(
    private val note: String,
    val key: String
) {

    TRAFFIC_ZQUEUE(
        note = "트래픽 대기 Queue Key",
        key = "ktc:{%s}:zqueue"
    ),
    TRAFFIC_TOKENS(
        note = "트래픽 진입 Token Bucket Key",
        key = "ktc:{%s}:bucket"
    ),
    TRAFFIC_THRESHOLD(
        note = "트래픽 분당 임계치 Key",
        key = "ktc:{%s}:threshold"
    ),
    TRAFFIC_LAST_REFILL_TIME(
        note = "트래픽 Token 마지막 리필 시점 Key",
        key = "ktc:{%s}:last_refill_time"
    ),
    TRAFFIC_LAST_ENTRY_TIME(
        note = "트래픽 Token 마지막 진입 시점 Key",
        key = "ktc:{%s}:last_entry_time"
    ),
    TRAFFIC_ENTRY_COUNTER(
        note = "트래픽 진입 Counter Key",
        key = "ktc:{%s}:entry_counter"
    ),
    TRAFFIC_ACTIVATION_ZONES(
        note = "트래픽 제어 활성화 Zone 목록 Key",
        key = "ktc:activation:zones"
    )
    ;

    companion object {
        fun generate(zoneId: String): Map<TrafficCacheKey, String> {
            return entries.associateWith { it.key.format(zoneId) }
        }

        fun generateTrafficControlKeys(zoneId: String): List<String> {
            // !! 순서 중요 !!
            val keys = listOf(
                TRAFFIC_ZQUEUE,
                TRAFFIC_TOKENS,
                TRAFFIC_THRESHOLD,
                TRAFFIC_LAST_REFILL_TIME,
                TRAFFIC_LAST_ENTRY_TIME,
                TRAFFIC_ENTRY_COUNTER
            )
            return keys.map { it.key.format(zoneId) }
        }
    }

    fun getKey(zoneId: String): String {
        return this.key.format(zoneId)
    }

}