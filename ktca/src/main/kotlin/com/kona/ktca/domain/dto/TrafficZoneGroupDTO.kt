package com.kona.ktca.domain.dto

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.infrastructure.jdsl.JpqlPredicateGenerator.whereEqualTo
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneGroupEntity
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable

data class TrafficZoneGroupDTO(
    val groupId: String? = null,
    val name: String? = null,
    val order: Int? = null,
    val status: TrafficZoneGroupStatus? = null
) {
    fun toPredicatable(): Array<Predicatable?> {
        return arrayOf(
            groupId?.let { whereEqualTo(it, TrafficZoneGroupEntity::id) },
            name?.let { whereEqualTo(it, TrafficZoneGroupEntity::name) },
            order?.let { whereEqualTo(it, TrafficZoneGroupEntity::groupOrder) },
            status?.let { whereEqualTo(it, TrafficZoneGroupEntity::status) }
        )
    }
}