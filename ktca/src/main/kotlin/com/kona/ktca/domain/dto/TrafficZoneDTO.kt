package com.kona.ktca.domain.dto

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.infrastructure.jdsl.JpqlPredicateGenerator.whereEqualTo
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneEntity
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import java.time.LocalDateTime

data class TrafficZoneDTO(
    val zoneId: String? = null,
    val zoneAlias: String? = null,
    val threshold: Long? = null,
    val groupId: String? = null,
    val status: TrafficZoneStatus? = null,
    val activationTime: LocalDateTime? = null,
    val requesterId: Long? = null
) {

    var group: TrafficZoneGroup? = null

    fun applyGroup(group: TrafficZoneGroup): TrafficZoneDTO {
        this.group = group
        return this
    }

    fun toPredicatable(): Array<Predicatable?> {
        return arrayOf(
            zoneId?.let { whereEqualTo(it, TrafficZoneEntity::id) },
            status?.let { whereEqualTo(it, TrafficZoneEntity::status) }
        )
    }
}