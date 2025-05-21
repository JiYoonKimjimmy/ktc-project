package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.model.TrafficZone

interface TrafficZoneSavePort {

    suspend fun save(trafficZone: TrafficZone): TrafficZone

}