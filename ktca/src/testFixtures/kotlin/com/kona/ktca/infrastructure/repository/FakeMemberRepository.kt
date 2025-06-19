package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.port.outbound.MemberRepository
import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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
        return entities.values.find { checkPredicate(dto, it) }?.toDomain()
    }

    override suspend fun findPageByPredicate(dto: MemberDTO, pageable: PageableDTO): Page<Member> {
        val filteredList = entities.values
            .filter { checkPredicate(dto, it) }
            .toList()

        val totalElements = filteredList.size.toLong()
        val start = pageable.number * pageable.size
        val end = minOf(start + pageable.size, totalElements.toInt())

        val content = if (start < totalElements) {
            filteredList.subList(start, end).map { it.toDomain() }
        } else {
            emptyList()
        }

        return PageImpl(content, pageable.toPageRequest(), totalElements)
    }

    private fun checkPredicate(dto: MemberDTO, entity: MemberEntity): Boolean {
        return (dto.memberId?.let { it == entity.id } ?: true)
                && (dto.loginId?.let { it == entity.loginId } ?: true)
                && (dto.name?.let { it == entity.name } ?: true)
                && (dto.email?.let { it == entity.email } ?: true)
                && (dto.team?.let { it == entity.team } ?: true)
                && (dto.role?.let { it == entity.role } ?: true)
                && (dto.status?.let { it == entity.status } ?: true)
                && (dto.lastLoginAt?.let { it == entity.lastLoginAt } ?: true)
    }

    override suspend fun existsByLoginId(loginId: String): Boolean {
        return findByLoginId(loginId) != null
    }

}