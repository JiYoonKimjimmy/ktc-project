package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import java.time.LocalDateTime

object TrafficZoneMonitorFixture {

    fun giveOne(index: Int): TrafficZoneMonitor {
        return TrafficZoneMonitor(
            id= "$index",
            zoneId= "zoneId",
            zoneAlias= "zoneAlias",
            threshold= 1000,
            status= TrafficZoneStatus.ACTIVE,
            activationTime= LocalDateTime.now(),
            entryCount= index.toLong(),
            waitingCount= index.toLong(),
            estimatedClearTime= index.toLong() * 6000,
        )
    }

}
