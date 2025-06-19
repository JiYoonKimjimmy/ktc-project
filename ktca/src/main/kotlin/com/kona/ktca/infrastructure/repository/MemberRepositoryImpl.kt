package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import com.kona.ktca.infrastructure.repository.jpa.MemberJpaRepository
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryImpl(
    private val memberJpaRepository: MemberJpaRepository
) : MemberRepository {

    override suspend fun save(entity: MemberEntity): MemberEntity = withContext(Dispatchers.IO) {
        memberJpaRepository.save(entity)
    }

    override suspend fun findByMemberId(memberId: Long): MemberEntity? = withContext(Dispatchers.IO) {
        memberJpaRepository.findById(memberId).orElse(null)
    }

    override suspend fun findByLoginId(loginId: String): MemberEntity? = withContext(Dispatchers.IO) {
        memberJpaRepository.findByLoginId(loginId)
    }

    override suspend fun findByPredicate(dto: MemberDTO): MemberEntity? {
        return memberJpaRepository.findAll {
            select(entity(MemberEntity::class))
                .from(entity(MemberEntity::class))
                .whereAnd(*dto.toPredicatable())
        }.first()
    }

    override suspend fun existsByLoginId(loginId: String): Boolean = withContext(Dispatchers.IO) {
        memberJpaRepository.existsByLoginId(loginId)
    }

}