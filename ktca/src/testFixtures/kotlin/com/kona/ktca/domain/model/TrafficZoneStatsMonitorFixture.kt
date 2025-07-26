package com.kona.ktca.domain.model

import com.kona.ktca.dto.StatsType

const val BASIC_TEST_ZONE_ID = "TEST_FIXTURE_ZONE_ID"
const val BASIC_TEST_ZONE_ALIAS = "TEST_FIXTURE_ZONE_ALIS"

object TrafficZoneStatsMonitorFixture {

    fun giveOne(
        zoneId: String = BASIC_TEST_ZONE_ID,
        zoneAlias: String = BASIC_TEST_ZONE_ALIAS,

        threshold: Long = 0,
        entryCountPerDay: Long = 0,
        waitingCount: Long = 0,
        estimatedClearTime: Long = 0,

        statsType: StatsType = StatsType.MINUTE,
        statsDate: String? = null,
        maxThreshold: Long = 0,
        totalEntryCount: Long = 0,
        maxWaitingCount: Long = 0,
        maxEstimatedClearTime: Long = 0,
    ): TrafficZoneStatsMonitor {
        return TrafficZoneStatsMonitor(
            zoneId= zoneId,
            zoneAlias= zoneAlias
        ).apply {
            this.threshold = threshold
            this.entryCountPerDay = entryCountPerDay
            this.waitingCount = waitingCount
            this.estimatedClearTime = estimatedClearTime

            this.statsDate = statsDate ?: ""
            this.statsType = statsType
            this.maxThreshold = maxThreshold
            this.totalEntryCount = totalEntryCount
            this.maxWaitingCount = maxWaitingCount
            this.maxEstimatedClearTime = maxEstimatedClearTime
        }
    }
}
