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

@Table(name = "MEMBER_ZONE_LOG")
@Entity
class MemberZoneLogEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MemberLogType,
    @Column(nullable = false)
    val zoneId: String,
    @Column(nullable = false)
    val zoneAlias: String,
    @Column(nullable = false)
    val threshold: Long,
    @Column(nullable = false)
    val groupId: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TrafficZoneStatus,
    @Column(nullable = false)
    val activationTime: LocalDateTime,
    @Column(nullable = false)
    val zoneCreated: LocalDateTime,
    @Column(nullable = false)
    val zoneUpdated: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    val member: MemberEntity

) : BaseEntity() {

    companion object {
        fun of(domain: MemberLog): MemberZoneLogEntity {
            return MemberZoneLogEntity(
                type = domain.type,
                zoneId = domain.zoneLog.zoneId,
                zoneAlias = domain.zoneLog.zoneAlias,
                threshold = domain.zoneLog.threshold,
                groupId = domain.zoneLog.groupId,
                status = domain.zoneLog.status,
                activationTime = domain.zoneLog.activationTime,
                zoneCreated = domain.zoneLog.created ?: domain.zoneLog.updated ?: LocalDateTime.now(),
                zoneUpdated = domain.zoneLog.updated ?: LocalDateTime.now(),
                member = MemberEntity.of(domain.member)
            )
        }

        fun jpqlQuery(where: Array<Predicatable?>): Jpql.() -> JpqlQueryable<SelectQuery<MemberZoneLogEntity>> {
            val query: Jpql.() -> JpqlQueryable<SelectQuery<MemberZoneLogEntity>> = {
                select(entity(MemberZoneLogEntity::class))
                    .from(
                        entity(MemberZoneLogEntity::class),
                        fetchJoin(MemberZoneLogEntity::member)
                    )
                    .whereAnd(*where)
            }
            return query
        }
    }

    override fun toDomain(): MemberLog {
        return MemberLog(
            logId = id,
            member = member.toDomain(),
            type = type,
            zoneLog = generateZoneLog(),
            created = created,
            updated = updated,
        )
    }

    private fun generateZoneLog(): TrafficZone {
        return TrafficZone(
            zoneId = zoneId,
            zoneAlias = zoneAlias,
            threshold = threshold,
            status = status,
            activationTime = activationTime,
            created = zoneCreated,
            updated = zoneUpdated
        )
    }
}