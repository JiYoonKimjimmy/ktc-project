package com.kona.common.infrastructure.lock

enum class DistributedLockType(
    private val note: String,
    private val keySpec: String
) {

    EXPIRE_TRAFFIC_TOKEN_SCHEDULE_LOCK(
        note = "트래픽 토큰 만료 Scheduler 실행 Lock",
        keySpec = "ktc:{dateTime}:expire-traffic-token-schedule-lock"
    )
    ;

    fun getKey(vararg args: String): String {
        return when (this) {
            EXPIRE_TRAFFIC_TOKEN_SCHEDULE_LOCK -> {
                this.keySpec.replace("{dateTime}", args[0])
            }
        }
    }

}