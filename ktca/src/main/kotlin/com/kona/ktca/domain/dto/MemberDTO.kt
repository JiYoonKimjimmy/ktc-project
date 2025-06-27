package com.kona.ktca.domain.dto

import com.kona.common.infrastructure.enumerate.MemberRole
import com.kona.common.infrastructure.enumerate.MemberStatus
import com.kona.ktca.infrastructure.jdsl.JpqlPredicateGenerator.whereEqualTo
import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import java.time.LocalDateTime

data class MemberDTO(
    val memberId: Long? = null,
    val loginId: String? = null,
    val password: String? = null,
    val name: String? = null,
    val email: String? = null,
    val team: String? = null,
    val role: MemberRole? = null,
    val status: MemberStatus? = null,
    val lastLoginAt: LocalDateTime? = null,
) {
    val isNeedLoginIdDuplicateCheck: Boolean by lazy {
        loginId != null
    }

    fun toPredicatable(): Array<Predicatable?> {
        return arrayOf(
            memberId?.let { whereEqualTo(column = MemberEntity::id, value = it) },
            loginId?.let { whereEqualTo(column = MemberEntity::loginId, value = it) },
            name?.let { whereEqualTo(column = MemberEntity::name, value = it) },
            email?.let { whereEqualTo(column = MemberEntity::email, value = it) },
            team?.let { whereEqualTo(column = MemberEntity::team, value = it) },
            role?.let { whereEqualTo(column = MemberEntity::role, value = it) },
            status?.let { whereEqualTo(column = MemberEntity::status, value = it) },
        )
    }
}