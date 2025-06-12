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

    companion object {
        fun of(domain: TrafficZone): TrafficZoneEntity {
            return TrafficZoneEntity(
                id = domain.zoneId,
                alias = domain.zoneAlias,
                threshold = domain.threshold,
                status = domain.status,
                activationTime = domain.activationTime
            ).apply {
                this.created = domain.created
                this.updated = domain.updated
            }
        }
    }

    override fun toDomain(): TrafficZone {
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