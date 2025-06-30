package com.kona.ktca.domain.dto

import com.kona.common.infrastructure.enumerate.MemberLogType
import com.kona.ktca.infrastructure.jdsl.JpqlPredicateGenerator.greaterThanOrEqualTo
import com.kona.ktca.infrastructure.jdsl.JpqlPredicateGenerator.lessThanOrEqualTo
import com.kona.ktca.infrastructure.jdsl.JpqlPredicateGenerator.whereEqualTo
import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import com.kona.ktca.infrastructure.repository.entity.MemberZoneLogEntity
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Paths.path
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import java.time.LocalDateTime

data class MemberLogDTO(
    val memberId: Long? = null,
    val type: MemberLogType? = null,
    val fromDate: LocalDateTime,
    val toDate: LocalDateTime
) {
    fun toPredicatable(): Array<Predicatable?> {
        return arrayOf(
            whereEqualTo(column = path(path(MemberZoneLogEntity::member), MemberEntity::id), value = memberId),
            whereEqualTo(column = MemberZoneLogEntity::type, value = type),
            greaterThanOrEqualTo(property = MemberZoneLogEntity::created, value = fromDate),
            lessThanOrEqualTo(property = MemberZoneLogEntity::created, value = toDate),
        )
    }
}