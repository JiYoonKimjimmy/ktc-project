package com.kona.ktca.domain.event

data class TrafficZoneMonitoringStoppedEvent(
    val zoneIds: List<String>
)