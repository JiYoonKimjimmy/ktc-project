package com.kona.ktca.application.usecase

import com.kona.common.infrastructure.enumerate.MemberLogType
import com.kona.common.infrastructure.enumerate.MemberLogType.*
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.common.infrastructure.util.DEFAULT_MEMBER_ID
import com.kona.common.infrastructure.util.DEFAULT_ZONE_GROUP_NAME
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import com.kona.ktca.domain.event.TrafficZoneChangedEvent
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.port.inbound.TrafficZoneFindPort
import com.kona.ktca.domain.port.inbound.TrafficZoneGroupFindPort
import com.kona.ktca.domain.port.inbound.TrafficZoneGroupSavePort
import com.kona.ktca.domain.port.inbound.TrafficZoneSavePort
import com.kona.ktca.domain.port.outbound.TrafficZoneCachingPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

@Component
class TrafficZoneManagementUseCase(
    private val trafficZoneSavePort: TrafficZoneSavePort,
    private val trafficZoneFindPort: TrafficZoneFindPort,
    private val trafficZoneGroupFindPort: TrafficZoneGroupFindPort,
    private val trafficZoneGroupSavePort: TrafficZoneGroupSavePort,
    private val trafficZoneCachingPort: TrafficZoneCachingPort,
    private val eventPublisher: ApplicationEventPublisher
) {

    suspend fun createTrafficZone(dto: TrafficZoneDTO): TrafficZone {
        // 요청 `zoneId` 포함되는 경우, `zoneId` 중복 검증 처리
        dto.zoneId?.let { trafficZoneFindPort.validateTrafficZoneId(it) }
        // 요청 `groupId` 기준, Zone 그룹 정보 조회
        dto.applyGroup(findTrafficZoneGroup(dto.groupId))
        // Zone 신규 생성 처리
        return trafficZoneSavePort.create(dto)
            .publishTrafficZoneChangedEvent(requesterId = dto.requesterId, type = TRAFFIC_ZONE_CREATED)
    }

    suspend fun findTrafficZone(zoneId: String): TrafficZone {
        return trafficZoneFindPort.findTrafficZone(zoneId)
    }

    suspend fun findPageTrafficZone(trafficZone: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone> {
        return trafficZoneFindPort.findPageTrafficZone(trafficZone, pageable)
    }

    suspend fun updateTrafficZone(zoneId: String, dto: TrafficZoneDTO): TrafficZone {
        // 요청 `zoneId` 기준 트래픽 제어 Zone 조회
        val zone = trafficZoneFindPort.findTrafficZone(zoneId)
        // 요청 `groupId` or Zone `groupId` 기준, Zone 그룹 정보 조회
        dto.applyGroup(findTrafficZoneGroup(dto.groupId ?: zone.groupId))
        // Zone 정보 업데이트 처리
        return trafficZoneSavePort.update(zone, dto)
            .publishTrafficZoneChangedEvent(requesterId = dto.requesterId, type = TRAFFIC_ZONE_UPDATED)
    }

    suspend fun deleteTrafficZone(zoneId: String, requesterId: Long?) {
        trafficZoneSavePort.delete(zoneId)
            .publishTrafficZoneChangedEvent(requesterId = requesterId, type = TRAFFIC_ZONE_DELETED)
    }

    suspend fun clearTrafficZone(zoneIds: List<String>) {
        trafficZoneCachingPort.clearAll(zoneIds)
    }

    private suspend fun findTrafficZoneGroup(groupId: String?): TrafficZoneGroup {
        // 요청 `groupId` or `group name : "기본 그룹"` 조회
        return groupId
            ?.let { trafficZoneGroupFindPort.findTrafficZoneGroup(TrafficZoneGroupDTO(groupId = it)) }
            ?: findDefaultTrafficZoneGroup()
    }

    private suspend fun findDefaultTrafficZoneGroup(): TrafficZoneGroup {
        // `group name : "기본 그룹"` 인 그룹 조회, 없는 경우 "기본 그룹" 신규 등록
        return try {
            trafficZoneGroupFindPort.findTrafficZoneGroup(TrafficZoneGroupDTO(name = DEFAULT_ZONE_GROUP_NAME))
        } catch (e: ResourceNotFoundException) {
            trafficZoneGroupSavePort.create(DEFAULT_ZONE_GROUP_NAME)
        }
    }

    private suspend fun TrafficZone.publishTrafficZoneChangedEvent(requesterId: Long?, type: MemberLogType): TrafficZone {
        val event = TrafficZoneChangedEvent(memberId = requesterId ?: DEFAULT_MEMBER_ID, type = type, zone = this)
        eventPublisher.publishEvent(event)
        return this
    }

}