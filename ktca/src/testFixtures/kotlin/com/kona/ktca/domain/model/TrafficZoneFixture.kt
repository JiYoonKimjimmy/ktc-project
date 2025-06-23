package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.common.infrastructure.util.TRAFFIC_ZONE_ID_PREFIX
import java.time.LocalDateTime

class TrafficZoneFixture {

    fun giveOne(zoneId: String? = null): TrafficZone {
        return TrafficZone(
            zoneId = zoneId ?: (TRAFFIC_ZONE_ID_PREFIX + SnowflakeIdGenerator.generate()),
            zoneAlias = "test-zone",
            threshold = 1000,
            status = TrafficZoneStatus.ACTIVE,
            activationTime = LocalDateTime.now(),
            created = LocalDateTime.now(),
            updated = LocalDateTime.now()
        )
    }

}