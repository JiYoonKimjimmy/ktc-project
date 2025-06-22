package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.domain.port.outbound.MemberZoneLogRepository
import com.kona.ktca.infrastructure.repository.entity.MemberZoneLogEntity
import com.kona.ktca.infrastructure.repository.jpa.MemberZoneLogJpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

@Repository
class MemberZoneLogRepositoryImpl(
    private val memberZoneLogJpaRepository: MemberZoneLogJpaRepository
) : MemberZoneLogRepository {

    override suspend fun save(log: MemberLog): MemberLog = withContext(Dispatchers.IO) {
        memberZoneLogJpaRepository.save(MemberZoneLogEntity.of(domain = log)).toDomain()
    }

}