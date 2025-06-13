package com.kona.common.infrastructure.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

const val DATE_BASIC_PATTERN = "yyyyMMdd"
const val DATE_TIME_BASIC_PATTERN = "yyyyMMddHHmmss"
const val DATE_TIME_PATTERN_yyyyMMddHHmm = "yyyyMMddHHmm"
const val DATE_TIME_PATTERN_yyMMddHHmmss = "yyMMddHHmmss"

fun LocalDate.convertPatternOf(pattern: String = DATE_BASIC_PATTERN): String {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}

fun LocalDateTime.convertPatternOf(pattern: String = DATE_TIME_BASIC_PATTERN): String {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}

fun LocalDateTime.convertUTCEpochTime(): String {
    val utcZoned = this.atZone(ZoneOffset.systemDefault()).withZoneSameInstant(ZoneOffset.UTC)
    return utcZoned.toInstant().toEpochMilli().toString()
}

fun String.convertPatternOf(pattern: String = DATE_TIME_BASIC_PATTERN): LocalDateTime {
    return LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))
}