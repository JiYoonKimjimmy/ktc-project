package com.kona.ktca.infrastructure.repository.jpa

import com.kona.ktca.infrastructure.repository.entity.MemberZoneLogEntity
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository

interface MemberZoneLogJpaRepository : JpaRepository<MemberZoneLogEntity, Long>, KotlinJdslJpqlExecutor {

    fun findAllByMemberId(memberId: Long): List<MemberZoneLogEntity>

}