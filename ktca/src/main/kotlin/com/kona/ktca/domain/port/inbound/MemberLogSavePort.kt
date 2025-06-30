package com.kona.ktca.domain.port.inbound

import com.kona.common.infrastructure.enumerate.MemberLogType
import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.domain.model.TrafficZone

interface MemberLogSavePort {

    suspend fun create(memberId: Long, type: MemberLogType, zone: TrafficZone) : MemberLog

}