package com.kona.common.infrastructure.error

enum class FeatureCode(
    val code: String,
    val message: String
) {

    UNKNOWN("9999", "Unknown Service"),
    FAULTY("0000", "Service Faulty Response"),

    V1_TRAFFIC_CONTROL_SERVICE("1001", "Traffic Control Service"),
    V1_TRAFFIC_ZONE_MANAGEMENT_SERVICE("1002", "Traffic Zone Management Service"),
    V1_TRAFFIC_ZONE_MONITORING_SERVICE("1003", "Traffic Zone Monitoring Service"),
    V1_MEMBER_MANAGEMENT_SERVICE("1004", "Member Management Service"),
    V1_TRAFFIC_ZONE_GROUP_MANAGEMENT_SERVICE("1005", "Traffic Zone Group Management Service"),

}