package com.kona.ktca.infrastructure.repository.entity

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.ktca.domain.model.TrafficZoneMonitor
import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "KTC_TRAFFIC_ZONE_MONITORING")
@Entity
class TrafficZoneMonitorEntity(

    @Id
    val id: String,
    val zoneId: String,
    val zoneAlias: String,
    val threshold: Long,
    @Enumerated(EnumType.STRING)
    val status: TrafficZoneStatus,
    val activationTime: LocalDateTime,
    val entryCount: Long,
    val waitingCount: Long,
    val estimatedClearTime: Long,

) : BaseEntity() {

    companion object {
        fun of(domain: TrafficZoneMonitor): TrafficZoneMonitorEntity {
            return TrafficZoneMonitorEntity(
                   id = domain.id ?: SnowflakeIdGenerator.generate(),
                zoneId = domain.zoneId,
                zoneAlias = domain.zoneAlias,
                threshold = domain.threshold,
                status = domain.status,
                activationTime = domain.activationTime,
                entryCount = domain.entryCount,
                waitingCount = domain.waitingCount,
                estimatedClearTime = domain.estimatedClearTime
            )
        }
    }

    override fun toDomain(): TrafficZoneMonitor {
        return TrafficZoneMonitor(
            id = id,
            zoneId = zoneId,
            zoneAlias = zoneAlias,
            threshold = threshold,
            status = status,
            activationTime = activationTime,
            entryCount = entryCount,
            waitingCount = waitingCount,
            estimatedClearTime = estimatedClearTime
        )
    }

}