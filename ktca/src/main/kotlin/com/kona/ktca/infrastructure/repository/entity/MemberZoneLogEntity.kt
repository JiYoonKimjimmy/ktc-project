package com.kona.ktca.infrastructure.repository.entity

import com.kona.common.infrastructure.enumerate.MemberLogType
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.domain.model.TrafficZone
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Table(name = "KTC_MEMBER_ZONE_LOG")
@Entity
class MemberZoneLogEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    val memberId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MemberLogType,
    @Column(nullable = false)
    val zoneId: String,
    @Column(nullable = false)
    val zoneAlias: String,
    @Column(nullable = false)
    val threshold: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TrafficZoneStatus,
    @Column(nullable = false)
    val activationTime: LocalDateTime,
    @Column(nullable = false)
    val zoneCreated: LocalDateTime,
    @Column(nullable = false)
    val zoneUpdated: LocalDateTime

) : BaseEntity() {

    companion object {
        fun of(domain: MemberLog): MemberZoneLogEntity {
            return MemberZoneLogEntity(
                memberId = domain.memberId,
                type = domain.type,
                zoneId = domain.zone.zoneId,
                zoneAlias = domain.zone.zoneAlias,
                threshold = domain.zone.threshold,
                status = domain.zone.status,
                activationTime = domain.zone.activationTime,
                zoneCreated = domain.zone.created!!,
                zoneUpdated = domain.zone.updated!!
            )
        }
    }

    override fun toDomain(): MemberLog {
        return MemberLog(
            memberId = memberId,
            type = type,
            zone = TrafficZone(
                zoneId = zoneId,
                zoneAlias = zoneAlias,
                threshold = threshold,
                status = status,
                activationTime = activationTime,
                created = zoneCreated,
                updated = zoneUpdated
            )
        )
    }
}