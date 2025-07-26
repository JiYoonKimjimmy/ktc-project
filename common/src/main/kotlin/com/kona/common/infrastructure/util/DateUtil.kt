package com.kona.common.infrastructure.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

const val DATE_PATTERN_yyyyMM = "yyyyMM"
const val DATE_BASIC_PATTERN = "yyyyMMdd"

const val DATE_TIME_PATTERN_yyyyMMddHH = "yyyyMMddHH"
const val DATE_TIME_PATTERN_yyyyMMddHHmm = "yyyyMMddHHmm"
const val DATE_TIME_BASIC_PATTERN = "yyyyMMddHHmmss"
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

fun LocalDateTime.convertPatternAndAddUnitOf(pattern: String = DATE_TIME_BASIC_PATTERN, unit: Long): String {
    return when (pattern) {
        DATE_PATTERN_yyyyMM             -> this.plusMonths(unit).convertPatternOf(pattern)
        DATE_BASIC_PATTERN              -> this.plusDays(unit).convertPatternOf(pattern)
        DATE_TIME_PATTERN_yyyyMMddHH    -> this.plusHours(unit).convertPatternOf(pattern)
        DATE_TIME_PATTERN_yyyyMMddHHmm  -> this.plusMinutes(unit).convertPatternOf(pattern)
        DATE_TIME_PATTERN_yyMMddHHmmss  -> this.plusSeconds(unit).convertPatternOf(pattern)
        DATE_TIME_BASIC_PATTERN         -> this.plusSeconds(unit).convertPatternOf(pattern)
        else -> throw IllegalArgumentException("날짜 변환에 지원되지 않는 패턴: $pattern")
    }
}

fun convertFirstDayOfMonth(yyyyMM: String): String {
    val yearMonth = YearMonth.parse(yyyyMM, DateTimeFormatter.ofPattern(DATE_PATTERN_yyyyMM))
    val firstDayOfMonth = yearMonth.atDay(1)
    return firstDayOfMonth.format(DateTimeFormatter.ofPattern(DATE_BASIC_PATTERN))
}

fun convertLastDayOfMonth(yyyyMM: String): String {
    val yearMonth = YearMonth.parse(yyyyMM, DateTimeFormatter.ofPattern(DATE_PATTERN_yyyyMM))
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    return lastDayOfMonth.format(DateTimeFormatter.ofPattern(DATE_BASIC_PATTERN))
}