package com.kona.ktca.domain.dto

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.infrastructure.jdsl.JpqlPredicateGenerator.whereEqualTo
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneEntity
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import java.time.LocalDateTime

data class TrafficZoneDTO(
    val zoneId: String? = null,
    val zoneAlias: String? = null,
    val threshold: Long? = null,
    val status: TrafficZoneStatus? = null,
    val activationTime: LocalDateTime? = null,
) {
    fun toPredicatable(): Array<Predicatable?> {
        return arrayOf(
            zoneId?.let { whereEqualTo(it, TrafficZoneEntity::id) },
            status?.let { whereEqualTo(it, TrafficZoneEntity::status) }
        )
    }
}