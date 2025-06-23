package com.kona.ktca.application.usecase

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.port.inbound.TrafficZoneGroupFindPort
import com.kona.ktca.domain.port.inbound.TrafficZoneGroupSavePort
import org.springframework.stereotype.Component

@Component
class TrafficZoneGroupManagementUseCase(
    private val trafficZoneGroupSavePort: TrafficZoneGroupSavePort,
    private val trafficZoneGroupFindPort: TrafficZoneGroupFindPort
) {

    suspend fun createTrafficZoneGroup(name: String): Long {
        return trafficZoneGroupSavePort.create(name).groupId!!
    }

    suspend fun findAllTrafficZoneGroup(): List<TrafficZoneGroup> {
        return trafficZoneGroupFindPort.findAllTrafficZoneGroup()
    }

    suspend fun updateTrafficZoneGroup(dto: TrafficZoneGroupDTO): Long {
        return trafficZoneGroupSavePort.update(dto).groupId!!
    }

    suspend fun deleteTrafficZoneGroup(groupId: Long) {
        val dto = TrafficZoneGroupDTO(groupId = groupId, status = TrafficZoneGroupStatus.DELETED)
        trafficZoneGroupSavePort.update(dto)
    }

}