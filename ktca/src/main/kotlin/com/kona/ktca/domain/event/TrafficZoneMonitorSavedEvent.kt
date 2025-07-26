package com.kona.ktca.domain.event

import com.kona.ktca.domain.model.TrafficZoneStatsMonitor

data class TrafficZoneMonitorSavedEvent(
    val trafficZoneMonitors: List<TrafficZoneStatsMonitor>
)
