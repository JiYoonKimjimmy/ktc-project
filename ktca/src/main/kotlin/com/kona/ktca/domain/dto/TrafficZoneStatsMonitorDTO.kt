package com.kona.ktca.domain.dto

import com.kona.ktca.dto.StatsType
import com.kona.ktca.infrastructure.jdsl.JpqlPredicateGenerator.whereEqualTo
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneStatsMonitorEntity
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneStatsMonitorId
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Paths.path
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicates.greaterThanOrEqualTo
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicates.lessThanOrEqualTo

data class TrafficZoneStatsMonitorDTO(
    val zoneId: String? = null,
    val statsType: StatsType? = null,
    val startDate: String? = null,
    val endDate: String? = null,
) {

    fun toPredicatable(): Array<Predicatable?> {
        val statsDatePath = path(path(TrafficZoneStatsMonitorEntity::id), TrafficZoneStatsMonitorId::statsDate)

        return arrayOf(
            zoneId?.let {whereEqualTo(
                column = path(path(TrafficZoneStatsMonitorEntity::id), TrafficZoneStatsMonitorId::zoneId),
                value = zoneId
            )},
            statsType?.let { whereEqualTo(path(TrafficZoneStatsMonitorEntity::statsType), statsType) },
            startDate?.let { greaterThanOrEqualTo(statsDatePath, Expressions.value(startDate)) },
            endDate?.let { lessThanOrEqualTo(statsDatePath, Expressions.value(endDate)) }
        )
    }
}