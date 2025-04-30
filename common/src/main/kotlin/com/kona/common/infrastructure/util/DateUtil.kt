package com.kona.common.infrastructure.util

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

const val DATE_TIME_BASIC_PATTERN = "yyyyMMddHHmmSS"
const val DATE_TIME_PATTERN_yyyyMMddHHmm = "yyyyMMddHHmm"

fun LocalDateTime.convertPatternOf(pattern: String = DATE_TIME_BASIC_PATTERN): String {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}

fun LocalDateTime.toInstantEpochMilli(): String {
    return this.toInstant(ZoneOffset.UTC).toEpochMilli().toString()
}