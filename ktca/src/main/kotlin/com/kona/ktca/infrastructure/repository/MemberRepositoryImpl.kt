package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.port.outbound.MemberRepository
import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import com.kona.ktca.infrastructure.repository.jpa.MemberJpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryImpl(
    private val memberJpaRepository: MemberJpaRepository
) : MemberRepository {

    override suspend fun save(member: Member): Member = withContext(Dispatchers.IO) {
        memberJpaRepository.save(MemberEntity.of(domain = member)).toDomain()
    }

    override suspend fun findByMemberId(memberId: Long): Member? = withContext(Dispatchers.IO) {
        memberJpaRepository.findById(memberId).orElse(null)?.toDomain()
    }

    override suspend fun findByLoginId(loginId: String): Member? = withContext(Dispatchers.IO) {
        memberJpaRepository.findByLoginId(loginId)?.toDomain()
    }

    override suspend fun findByPredicate(dto: MemberDTO): Member? = withContext(Dispatchers.IO) {
        val query = MemberEntity.jpqlQuery(dto.toPredicatable())
        memberJpaRepository.findAll(0, 1, query).first()?.toDomain()
    }

    override suspend fun findPageByPredicate(dto: MemberDTO, pageable: PageableDTO): Page<Member> = withContext(Dispatchers.IO) {
        val query = MemberEntity.jpqlQuery(dto.toPredicatable())
        val result = memberJpaRepository.findPage(pageable.toPageRequest(), query)
        PageImpl(result.content.mapNotNull { it?.toDomain() }, result.pageable, result.totalElements)
    }

    override suspend fun existsByLoginId(loginId: String): Boolean = withContext(Dispatchers.IO) {
        memberJpaRepository.existsByLoginId(loginId)
    }

}