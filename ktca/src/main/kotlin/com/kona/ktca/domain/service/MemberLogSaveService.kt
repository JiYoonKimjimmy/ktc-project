package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.MemberLogType
import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.inbound.MemberLogSavePort
import com.kona.ktca.domain.port.outbound.MemberZoneLogRepository
import org.springframework.stereotype.Service

@Service
class MemberLogSaveService(
    private val memberZoneLogRepository: MemberZoneLogRepository
) : MemberLogSavePort {

    override suspend fun create(
        memberId: Long,
        type: MemberLogType,
        zone: TrafficZone,
    ) : MemberLog {
        val log = MemberLog(memberId = memberId, type = type).applyZoneLog(zone)
        return memberZoneLogRepository.save(log)
    }

}