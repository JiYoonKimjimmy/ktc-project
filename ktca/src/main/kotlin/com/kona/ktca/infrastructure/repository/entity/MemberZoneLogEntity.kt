package com.kona.ktca.infrastructure.repository.entity

import com.kona.common.infrastructure.enumerate.MemberLogType
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.domain.model.TrafficZone
import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.JpqlQueryable
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.querymodel.jpql.select.SelectQuery
import jakarta.persistence.*
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
                zoneId = domain.zoneLog.zoneId,
                zoneAlias = domain.zoneLog.zoneAlias,
                threshold = domain.zoneLog.threshold,
                status = domain.zoneLog.status,
                activationTime = domain.zoneLog.activationTime,
                zoneCreated = domain.zoneLog.created ?: domain.zoneLog.updated ?: LocalDateTime.now(),
                zoneUpdated = domain.zoneLog.updated ?: LocalDateTime.now()
            )
        }

        fun jpqlQuery(where: Array<Predicatable?>): Jpql.() -> JpqlQueryable<SelectQuery<MemberZoneLogEntity>> {
            val query: Jpql.() -> JpqlQueryable<SelectQuery<MemberZoneLogEntity>> = {
                select(entity(MemberZoneLogEntity::class))
                    .from(entity(MemberZoneLogEntity::class))
                    .whereAnd(*where)
            }
            return query
        }
    }

    override fun toDomain(): MemberLog {
        val zone = TrafficZone(
            zoneId = zoneId,
            zoneAlias = zoneAlias,
            threshold = threshold,
            status = status,
            activationTime = activationTime,
            created = zoneCreated,
            updated = zoneUpdated
        )
        val log = MemberLog(
            logId = id,
            memberId = memberId,
            type = type,
            created = created,
            updated = updated,
        )
        return log.applyZoneLog(zone)
    }
}