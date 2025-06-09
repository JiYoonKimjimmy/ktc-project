package com.kona.ktca.infrastructure.repository.entity

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.domain.model.TrafficZone
import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "KTC_TRAFFIC_ZONES")
@Entity
class TrafficZoneEntity(

    @Id
    val id: String,
    val alias: String,
    val threshold: Long,
    @Enumerated(EnumType.STRING)
    val status: TrafficZoneStatus,
    val activationTime: LocalDateTime,

) : BaseEntity() {

    constructor(domain: TrafficZone): this(
        id = domain.zoneId,
        alias = domain.zoneAlias,
        threshold = domain.threshold,
        activationTime = domain.activationTime,
        status = domain.status
    ) {
        this.created = domain.created
        this.updated = domain.updated
    }

    fun toDomain(): TrafficZone {
        return TrafficZone(
            zoneId = id,
            zoneAlias = alias,
            threshold = threshold,
            activationTime = activationTime,
            status = status,
            created = created,
            updated = updated
        )
    }

}