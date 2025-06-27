package com.kona.ktca.infrastructure.repository.entity

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.domain.model.TrafficZone
import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.JpqlQueryable
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.querymodel.jpql.select.SelectQuery
import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "TRAFFIC_ZONES")
@Entity
class TrafficZoneEntity(

    @Id
    val id: String,
    @Column(nullable = false)
    val alias: String,
    @Column(nullable = false)
    val threshold: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TrafficZoneStatus,
    @Column(nullable = false)
    val activationTime: LocalDateTime,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    val group: TrafficZoneGroupEntity

) : BaseEntity() {


    companion object {
        fun of(domain: TrafficZone): TrafficZoneEntity {
            return TrafficZoneEntity(
                id = domain.zoneId,
                alias = domain.zoneAlias,
                threshold = domain.threshold,
                status = domain.status,
                activationTime = domain.activationTime,
                group = TrafficZoneGroupEntity.of(domain.group!!),
            ).apply {
                this.created = domain.created
                this.updated = domain.updated
            }
        }

        fun jpqlQuery(where: Array<Predicatable?>): Jpql.() -> JpqlQueryable<SelectQuery<TrafficZoneEntity>> {
            val query: Jpql.() -> JpqlQueryable<SelectQuery<TrafficZoneEntity>> = {
                select(entity(TrafficZoneEntity::class))
                    .from(entity(TrafficZoneEntity::class))
                    .whereAnd(*where)
            }
            return query
        }
    }

    override fun toDomain(): TrafficZone {
        return TrafficZone(
            zoneId = id,
            zoneAlias = alias,
            threshold = threshold,
            status = status,
            activationTime = activationTime,
            created = created,
            updated = updated,
            group = group.toDomain(),
        )
    }

}