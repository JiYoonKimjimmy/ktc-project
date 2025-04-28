package com.kona.common.infrastructure.error

enum class FeatureCode(
    val code: String,
    val message: String
) {

    UNKNOWN("9999", "Unknown Service"),

    V1_TRAFFIC_CONTROL_SERVICE("1001", "Traffic Control Service"),

}