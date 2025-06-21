package com.kona.ktca.infrastructure.repository.entity

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.domain.model.TrafficZone
import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.JpqlQueryable
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.querymodel.jpql.select.SelectQuery
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
            activationTime = activationTime,
            status = status,
            created = created,
            updated = updated
        )
    }

}