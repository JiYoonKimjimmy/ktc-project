package com.kona.ktca.v1.domain.port.outbound

import com.kona.ktca.v1.domain.model.TrafficZone

interface TrafficZoneSavePort {

    suspend fun save(trafficZone: TrafficZone): TrafficZone

}