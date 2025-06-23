package com.kona.ktca.infrastructure.repository.jpa

import com.kona.ktca.infrastructure.repository.entity.TrafficZoneGroupEntity
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository

interface TrafficZoneGroupJpaRepository : JpaRepository<TrafficZoneGroupEntity, Long>, KotlinJdslJpqlExecutor