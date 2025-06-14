package com.kona.ktca.application.event

data class ExpireTrafficZoneEntryCountEvent(
    val zoneIds: List<String>
)