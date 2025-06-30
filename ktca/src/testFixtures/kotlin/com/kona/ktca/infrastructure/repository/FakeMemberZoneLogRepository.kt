package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.dto.MemberLogDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.domain.port.outbound.MemberZoneLogRepository
import com.kona.ktca.infrastructure.repository.entity.MemberZoneLogEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class FakeMemberZoneLogRepository : MemberZoneLogRepository {
    private val entities = ConcurrentHashMap<Long, MemberZoneLogEntity>()
    private val idGenerator = AtomicLong(1)

    override suspend fun save(log: MemberLog): MemberLog {
        val entity = MemberZoneLogEntity.of(log)
        if (entity.id == null) {
            entity.id = idGenerator.getAndIncrement()
            entity.created = LocalDateTime.now()
            entity.updated = entity.created
        } else {
            entity.updated = LocalDateTime.now()
        }
        entities[entity.id!!] = entity
        return entity.toDomain()
    }

    override suspend fun findPageByPredicate(dto: MemberLogDTO, pageable: PageableDTO): Page<MemberLog> {
        val filtered = entities.values.filter { it -> checkPredicate(dto, it) }.sortedByDescending { it.created }
        val totalElements = filtered.size.toLong()
        val start = pageable.number * pageable.size
        val end = minOf(start + pageable.size, totalElements.toInt())
        val content = if (start < totalElements) {
            filtered.subList(start, end).map { it.toDomain() }
        } else {
            emptyList()
        }
        return PageImpl(content, pageable.toPageRequest(), totalElements)
    }

    private fun checkPredicate(dto: MemberLogDTO, entity: MemberZoneLogEntity): Boolean {
        return (dto.memberId?.let { it == entity.member.id } ?: true)
                && (dto.loginId?.let { it == entity.member.loginId } ?: true)
                && (dto.type?.let { it == entity.type } ?: true)
                && (dto.fromDate.let { entity.created?.isBefore(it) } ?: true)
                && (dto.toDate.let { entity.created?.isAfter(it) } ?: true)
    }

}
