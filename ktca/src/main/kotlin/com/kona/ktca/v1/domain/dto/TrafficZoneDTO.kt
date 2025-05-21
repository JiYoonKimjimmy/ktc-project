package com.kona.ktca.v1.domain.dto

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.v1.domain.model.TrafficZone
import com.kona.ktca.v1.infrastructure.jdsl.JpqlPredicateGenerator.whereEqualTo
import com.kona.ktca.v1.infrastructure.repository.entity.TrafficZoneEntity
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import java.time.LocalDateTime

data class TrafficZoneDTO(
    val zoneId: String? = null,
    val zoneAlias: String? = null,
    val threshold: Long? = null,
    val activationTime: LocalDateTime? = null,
    val status: TrafficZoneStatus? = null,
) {
    val isCreate: Boolean by lazy { zoneId == null }

    fun toDomain(): TrafficZone {
        return TrafficZone(
            zoneAlias = zoneAlias!!,
            threshold = threshold!!,
            activationTime = activationTime!!,
            status = status!!
        )
    }

    fun toPredicatable(): Array<Predicatable?> {
        return arrayOf(
            zoneId?.let { whereEqualTo(it, TrafficZoneEntity::id) },
            status?.let { whereEqualTo(it, TrafficZoneEntity::status) }
        )
    }

}