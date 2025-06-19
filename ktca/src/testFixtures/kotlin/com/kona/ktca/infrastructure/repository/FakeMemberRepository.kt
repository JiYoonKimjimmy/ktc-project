package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.port.outbound.MemberRepository
import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class FakeMemberRepository : MemberRepository {

    private val entities = ConcurrentHashMap<Long, MemberEntity>()

    override suspend fun save(member: Member): Member {
        val entity = MemberEntity.of(domain = member)
        if (entity.id == null) {
            entity.id = entities.keys.maxOrNull()?.plus(1) ?: 1L
            entity.created = LocalDateTime.now()
            entity.updated = LocalDateTime.now()
        } else {
            entity.updated = LocalDateTime.now()
        }
        entities[entity.id!!] = entity
        return entity.toDomain()
    }

    override suspend fun findByMemberId(memberId: Long): Member? {
        return entities[memberId]?.toDomain()
    }

    override suspend fun findByLoginId(loginId: String): Member? {
        return entities.values.find { it.loginId == loginId }?.toDomain()
    }

    override suspend fun findByPredicate(dto: MemberDTO): Member? {
        return entities.values.find { entity ->
            (dto.memberId == null || entity.id == dto.memberId) &&
            (dto.loginId == null || entity.loginId == dto.loginId) &&
            (dto.name == null || entity.name == dto.name) &&
            (dto.email == null || entity.email == dto.email) &&
            (dto.team == null || entity.team == dto.team) &&
            (dto.role == null || entity.role == dto.role) &&
            (dto.status == null || entity.status == dto.status) &&
            (dto.lastLoginAt == null || entity.lastLoginAt == dto.lastLoginAt)
        }?.toDomain()
    }

    override suspend fun existsByLoginId(loginId: String): Boolean {
        return findByLoginId(loginId) != null
    }

}