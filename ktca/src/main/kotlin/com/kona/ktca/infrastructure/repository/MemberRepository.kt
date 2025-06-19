package com.kona.ktca.infrastructure.repository

import com.kona.ktca.infrastructure.repository.entity.MemberEntity

interface MemberRepository {

    suspend fun save(entity: MemberEntity): MemberEntity

    suspend fun findByMemberId(memberId: Long): MemberEntity?

    suspend fun findByLoginId(loginId: String): MemberEntity?

    suspend fun existsByLoginId(loginId: String): Boolean

}