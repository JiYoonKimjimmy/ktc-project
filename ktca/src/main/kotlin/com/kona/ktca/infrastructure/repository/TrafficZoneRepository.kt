package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneEntity
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TrafficZoneRepository {

    suspend fun save(entity: TrafficZoneEntity): TrafficZoneEntity

    suspend fun findByZoneId(zoneId: String): TrafficZoneEntity?

    suspend fun findAllByStatus(status: TrafficZoneStatus): List<TrafficZoneEntity>

    suspend fun findPage(where: Array<Predicatable?>, pageable: Pageable): Page<TrafficZoneEntity?>

}