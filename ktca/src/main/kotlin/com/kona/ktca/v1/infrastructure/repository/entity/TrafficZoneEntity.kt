package com.kona.ktca.v1.infrastructure.repository.entity

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.v1.domain.model.TrafficZone
import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "TRAFFIC_ZONES")
@Entity
class TrafficZoneEntity(

    @Id
    val id: String,
    val alias: String,
    val threshold: Long,
    val activationTime: LocalDateTime,
    @Enumerated(EnumType.STRING)
    val status: TrafficZoneStatus,

) {

    constructor(domain: TrafficZone): this(
        id = domain.zoneId,
        alias = domain.zoneAlias,
        threshold = domain.threshold,
        activationTime = domain.activationTime,
        status = domain.status
    )

    fun toDomain(): TrafficZone {
        return TrafficZone(
            zoneId = id,
            zoneAlias = alias,
            threshold = threshold,
            activationTime = activationTime,
            status = status
        )
    }

}