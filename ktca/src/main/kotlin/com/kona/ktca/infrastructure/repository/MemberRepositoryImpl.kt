package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.port.outbound.MemberRepository
import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import com.kona.ktca.infrastructure.repository.jpa.MemberJpaRepository
import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.JpqlQueryable
import com.linecorp.kotlinjdsl.querymodel.jpql.select.SelectQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        val query: Jpql.() -> JpqlQueryable<SelectQuery<MemberEntity>> = {
            select(entity(MemberEntity::class))
                .from(entity(MemberEntity::class))
                .whereAnd(*dto.toPredicatable())
        }
        memberJpaRepository.findAll(0, 1, query).first()?.toDomain()
    }

    override suspend fun existsByLoginId(loginId: String): Boolean = withContext(Dispatchers.IO) {
        memberJpaRepository.existsByLoginId(loginId)
    }

}