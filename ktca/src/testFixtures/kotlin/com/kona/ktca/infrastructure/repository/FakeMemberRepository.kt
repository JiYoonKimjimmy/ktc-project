package com.kona.ktca.infrastructure.repository

import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import java.util.concurrent.ConcurrentHashMap

class FakeMemberRepository : MemberRepository {

    private val entities = ConcurrentHashMap<Long, MemberEntity>()

    override suspend fun save(entity: MemberEntity): MemberEntity {
        val id = entity.id ?: (entities.keys.maxOrNull()?.plus(1) ?: 1L)
        entity.id = id
        entities[id] = entity
        return entity
    }

    override suspend fun findByMemberId(memberId: Long): MemberEntity? {
        return entities[memberId]
    }

    override suspend fun findByLoginId(loginId: String): MemberEntity? {
        return entities.values.find { it.loginId == loginId }
    }
    
}