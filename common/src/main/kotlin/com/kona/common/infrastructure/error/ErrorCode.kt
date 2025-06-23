package com.kona.common.infrastructure.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {

    TRAFFIC_ZONE_NOT_FOUND(HttpStatus.NOT_FOUND, "100", "Traffic zone not found"),
    DELETED_TRAFFIC_ZONE_CANNOT_BE_CHANGED(HttpStatus.BAD_REQUEST, "101", "Deleted traffic zone cannot be changed"),
    TRAFFIC_ZONE_STATUS_IS_BLOCKED(HttpStatus.INTERNAL_SERVER_ERROR, "102", "Traffic zone status is blocked"),
    TRAFFIC_ZONE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "103", "Traffic zone already exists"),
    TRAFFIC_ZONE_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "104", "Traffic zone group not found"),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "200", "Member not found"),
    MEMBER_LOGIN_ID_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "201", "Member loginId already exists"),

    REQUIRED_REQUEST_PARAMETER_MISSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "900", "Required request parameter is missing"),
    EXTERNAL_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "901", "External API service error"),
    ARGUMENT_NOT_VALID_ERROR(HttpStatus.BAD_REQUEST, "902", "Argument not valid"),
    REDISSON_LOCK_ATTEMPT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "903", "Redisson lock attempt error"),

    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "999", "Unknown error"),

    FAULTY_503_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "503", "KTC Service unavailable"),
    
}
