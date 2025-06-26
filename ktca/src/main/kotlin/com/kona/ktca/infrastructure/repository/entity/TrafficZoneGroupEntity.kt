package com.kona.ktca.infrastructure.repository.entity

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.JpqlQueryable
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.querymodel.jpql.select.SelectQuery
import jakarta.persistence.*

@Table(
    name = "TRAFFIC_ZONE_GROUPS",
    uniqueConstraints = [ UniqueConstraint(name = "uk_zone_group_order", columnNames = ["groupOrder"]) ]
)
@Entity
class TrafficZoneGroupEntity(

    @Id
    var id: String,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val groupOrder: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TrafficZoneGroupStatus

) : BaseEntity() {

    companion object {
        fun of(domain: TrafficZoneGroup, nextOrder: Int? = null): TrafficZoneGroupEntity {
            return TrafficZoneGroupEntity(
                id = domain.groupId,
                name = domain.name,
                groupOrder = nextOrder ?: domain.order,
                status = domain.status
            ).apply {
                created = domain.created
                updated = domain.updated
            }
        }

        fun jpqlQuery(where: Array<Predicatable?>): Jpql.() -> JpqlQueryable<SelectQuery<TrafficZoneGroupEntity>> {
            val query: Jpql.() -> JpqlQueryable<SelectQuery<TrafficZoneGroupEntity>> = {
                select(entity(TrafficZoneGroupEntity::class))
                    .from(entity(TrafficZoneGroupEntity::class))
                    .whereAnd(*where)
            }
            return query
        }
    }

    override fun toDomain(): TrafficZoneGroup {
        return TrafficZoneGroup(
            groupId = id,
            name = name,
            order = groupOrder,
            status = status,
            created = created,
            updated = updated
        )
    }
}