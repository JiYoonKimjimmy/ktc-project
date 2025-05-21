package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneEntity
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.concurrent.ConcurrentHashMap

class FakeTrafficZoneRepository : TrafficZoneRepository {
    private val entities = ConcurrentHashMap<String, TrafficZoneEntity>()

    override suspend fun save(entity: TrafficZoneEntity): TrafficZoneEntity {
        entities[entity.id] = entity
        return entity
    }

    override suspend fun findByZoneId(zoneId: String): TrafficZoneEntity? {
        return entities[zoneId]
    }

    override suspend fun findAllByStatus(status: TrafficZoneStatus): List<TrafficZoneEntity> {
        return entities.values.filter { it.status == status }
    }

    override suspend fun findPage(where: Array<Predicatable?>, pageable: Pageable): Page<TrafficZoneEntity?> {
        TODO("Not yet implemented")
    }

}