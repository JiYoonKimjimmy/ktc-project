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
            memberId?.let { whereEqualTo(it, MemberEntity::id) },
            loginId?.let { whereEqualTo(it, MemberEntity::loginId) },
            name?.let { whereEqualTo(it, MemberEntity::name) },
            email?.let { whereEqualTo(it, MemberEntity::email) },
            team?.let { whereEqualTo(it, MemberEntity::team) },
            role?.let { whereEqualTo(it, MemberEntity::role) },
            status?.let { whereEqualTo(it, MemberEntity::status) },
        )
    }
}