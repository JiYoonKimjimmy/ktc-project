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
            groupId?.let { whereEqualTo(column = TrafficZoneGroupEntity::id, value = it) },
            name?.let { whereEqualTo(column = TrafficZoneGroupEntity::name, value = it) },
            order?.let { whereEqualTo(column = TrafficZoneGroupEntity::groupOrder, value = it) },
            status?.let { whereEqualTo(column = TrafficZoneGroupEntity::status, value = it) }
        )
    }
}