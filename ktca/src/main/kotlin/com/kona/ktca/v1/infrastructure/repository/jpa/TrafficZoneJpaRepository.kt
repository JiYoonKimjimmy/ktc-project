package com.kona.ktca.v1.infrastructure.repository.jpa

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.v1.infrastructure.repository.entity.TrafficZoneEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TrafficZoneJpaRepository : JpaRepository<TrafficZoneEntity, String> {

    fun findAllByStatus(status: TrafficZoneStatus): List<TrafficZoneEntity>

}