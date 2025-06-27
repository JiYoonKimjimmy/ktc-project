package com.kona.ktca.domain.port.outbound

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import org.springframework.data.domain.Page

interface TrafficZoneRepository {

    suspend fun save(zone: TrafficZone): TrafficZone

    suspend fun findByZoneId(zoneId: String): TrafficZone?

    suspend fun findByZoneIdAndStatusNot(zoneId: String, status: TrafficZoneStatus): TrafficZone?

    suspend fun findAllByPredicate(dto: TrafficZoneDTO): List<TrafficZone>

    suspend fun findPage(dto: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone>

    suspend fun deleteByZoneId(zoneId: String)

}