package com.kona.ktca.infrastructure.repository.jpa

import com.kona.ktca.infrastructure.repository.entity.TrafficZoneMonitorEntity
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository

interface TrafficZoneMonitorJpaRepository : JpaRepository<TrafficZoneMonitorEntity, String>, KotlinJdslJpqlExecutor