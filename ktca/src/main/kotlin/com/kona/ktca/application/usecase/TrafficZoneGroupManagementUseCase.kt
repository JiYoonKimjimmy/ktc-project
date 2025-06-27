package com.kona.ktca.application.usecase

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

    suspend fun createTrafficZoneGroup(name: String, order: Int?): String {
        return trafficZoneGroupSavePort.create(name, order).groupId
    }

    suspend fun findTrafficZoneGroup(groupId: String): TrafficZoneGroup {
        return trafficZoneGroupFindPort.findTrafficZoneGroup(TrafficZoneGroupDTO(groupId = groupId))
    }

    suspend fun findAllTrafficZoneGroup(): List<TrafficZoneGroup> {
        return trafficZoneGroupFindPort.findAllTrafficZoneGroup()
    }

    suspend fun updateTrafficZoneGroup(dto: TrafficZoneGroupDTO): String {
        return trafficZoneGroupSavePort.update(dto).groupId
    }

    suspend fun deleteTrafficZoneGroup(groupId: String) {
        trafficZoneGroupSavePort.delete(groupId)
    }

}