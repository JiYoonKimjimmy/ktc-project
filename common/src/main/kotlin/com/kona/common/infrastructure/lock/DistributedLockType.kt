package com.kona.common.infrastructure.lock

enum class DistributedLockType(
    private val note: String,
    private val key: String
) {

    EXPIRE_TRAFFIC_TOKEN_SCHEDULER_LOCK(
        note = "트래픽 토큰 만료 Scheduler 실행 Lock",
        key = "ktc:{dateTime}:expire-traffic-token-scheduler-lock"
    ),
    TRAFFIC_ZONE_MONITORING_SCHEDULER_LOCK(
        note = "트래픽 Zone 모니터링 수집 Scheduler 실행 Lock",
        key = "ktc:{dateTime}:traffic-zone-monitoring-scheduler-lock"
    )
    ;

    fun getKey(vararg args: String): String {
        return when (this) {
            EXPIRE_TRAFFIC_TOKEN_SCHEDULER_LOCK -> {
                this.key.replace("{dateTime}", args[0])
            }
            TRAFFIC_ZONE_MONITORING_SCHEDULER_LOCK -> {
                this.key.replace("{dateTime}", args[0])
            }
        }
    }

}