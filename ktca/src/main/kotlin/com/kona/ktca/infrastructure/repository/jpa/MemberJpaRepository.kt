package com.kona.ktca.infrastructure.repository.jpa

import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepository : JpaRepository<MemberEntity, Long>, KotlinJdslJpqlExecutor {

    fun findByLoginId(loginId: String): MemberEntity?

}