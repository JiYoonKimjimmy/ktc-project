package com.kona.ktca.infrastructure.repository.entity

import com.kona.ktca.domain.model.TrafficZoneStatsMonitor
import com.kona.ktca.dto.StatsType
import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.JpqlQueryable
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Paths
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.querymodel.jpql.select.SelectQuery
import jakarta.persistence.*
import java.io.Serializable

@Embeddable
data class TrafficZoneStatsMonitorId(
    val zoneId: String,
    val statsDate: String
): Serializable

@Table(name = "TRAFFIC_ZONE_STATS_MONITOR")
@Entity
data class TrafficZoneStatsMonitorEntity(

    @EmbeddedId
    val id: TrafficZoneStatsMonitorId,
    val zoneAlias: String,
    @Enumerated(EnumType.STRING)
    val statsType: StatsType,

    val maxThreshold: Long,
    val totalEntryCount: Long,
    val maxWaitingCount: Long,
    val maxEstimatedClearTime: Long,

) : BaseEntity() {

    companion object {
        fun of(domain: TrafficZoneStatsMonitor): TrafficZoneStatsMonitorEntity {
            return TrafficZoneStatsMonitorEntity(
                id = TrafficZoneStatsMonitorId(zoneId = domain.zoneId, statsDate = domain.statsDate!!),
                zoneAlias = domain.zoneAlias,
                statsType = domain.statsType!!,
                maxThreshold = domain.maxThreshold,
                totalEntryCount = domain.totalEntryCount,
                maxWaitingCount = domain.maxWaitingCount,
                maxEstimatedClearTime = domain.maxEstimatedClearTime
            )
        }

        fun jpqlQuery(where: Array<Predicatable?>): Jpql.() -> JpqlQueryable<SelectQuery<TrafficZoneStatsMonitorEntity>> {
            val query: Jpql.() -> JpqlQueryable<SelectQuery<TrafficZoneStatsMonitorEntity>> = {
                select(entity(TrafficZoneStatsMonitorEntity::class))
                    .from(entity(TrafficZoneStatsMonitorEntity::class))
                    .whereAnd(*where)
                    .orderBy(
                        Paths.path(
                            path(TrafficZoneStatsMonitorEntity::id), TrafficZoneStatsMonitorId::statsDate
                        ).asc())
            }
            return query
        }
    }

    override fun toDomain(): TrafficZoneStatsMonitor {
        return TrafficZoneStatsMonitor(
            zoneId = id.zoneId,
            zoneAlias = zoneAlias,
        ).apply {
            statsDate = id.statsDate
            statsType = this@TrafficZoneStatsMonitorEntity.statsType
            maxThreshold = this@TrafficZoneStatsMonitorEntity.maxThreshold
            totalEntryCount = this@TrafficZoneStatsMonitorEntity.totalEntryCount
            maxWaitingCount = this@TrafficZoneStatsMonitorEntity.maxWaitingCount
            maxEstimatedClearTime = this@TrafficZoneStatsMonitorEntity.maxEstimatedClearTime
        }
    }
}