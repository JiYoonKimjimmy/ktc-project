package com.kona.common.infrastructure.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val DATE_TIME_BASIC_PATTERN = "yyyyMMddHHmmss"
const val DATE_TIME_PATTERN_yyyyMMddHHmm = "yyyyMMddHHmm"
const val DATE_TIME_PATTERN_yyMMddHHmmss = "yyMMddHHmmss"

fun LocalDateTime.convertPatternOf(pattern: String = DATE_TIME_BASIC_PATTERN): String {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}

fun String.convertPatternOf(pattern: String = DATE_TIME_BASIC_PATTERN): LocalDateTime {
    return LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))
}

fun Instant.toTokenScore(): Long {
    val now = this.epochSecond
    return now * 1000 + (now % 1000)
}

fun Instant.toInstantEpochMilli(): String {
    return this.toEpochMilli().toString()
}