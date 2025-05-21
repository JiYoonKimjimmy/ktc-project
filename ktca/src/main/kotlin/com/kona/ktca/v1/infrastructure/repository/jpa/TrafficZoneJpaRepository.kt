package com.kona.ktca.v1.infrastructure.repository.jpa

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.v1.infrastructure.repository.entity.TrafficZoneEntity
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository

interface TrafficZoneJpaRepository : JpaRepository<TrafficZoneEntity, String>, KotlinJdslJpqlExecutor {

    fun findAllByStatus(status: TrafficZoneStatus): List<TrafficZoneEntity>

}