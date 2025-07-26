package com.kona.ktca.infrastructure.repository.jpa

import com.kona.ktca.infrastructure.repository.entity.TrafficZoneStatsMonitorEntity
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneStatsMonitorId
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository

interface TrafficZoneStatsMonitorJpaRepository : JpaRepository<TrafficZoneStatsMonitorEntity, String>, KotlinJdslJpqlExecutor {

    fun findAllByIdIn(ids: List<TrafficZoneStatsMonitorId>): List<TrafficZoneStatsMonitorEntity>
}