package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.common.infrastructure.util.TRAFFIC_ZONE_ID_PREFIX
import java.time.LocalDateTime

object TrafficZoneFixture {

    fun giveOne(
        zoneId: String? = null,
        group: TrafficZoneGroup = TrafficZoneGroupFixture.giveOne(),
        status: TrafficZoneStatus = TrafficZoneStatus.ACTIVE,
    ): TrafficZone {
        val zone = TrafficZone(
            zoneId = zoneId ?: (TRAFFIC_ZONE_ID_PREFIX + SnowflakeIdGenerator.generate()),
            zoneAlias = "test-zone",
            threshold = 1,
            group = group,
            status = status,
            activationTime = LocalDateTime.now(),
            created = LocalDateTime.now(),
            updated = LocalDateTime.now()
        )
        return zone
    }

}