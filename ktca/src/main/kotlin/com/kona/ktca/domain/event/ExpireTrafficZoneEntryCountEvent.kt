package com.kona.ktca.domain.event

data class ExpireTrafficZoneEntryCountEvent(
    val zoneIds: List<String>
)