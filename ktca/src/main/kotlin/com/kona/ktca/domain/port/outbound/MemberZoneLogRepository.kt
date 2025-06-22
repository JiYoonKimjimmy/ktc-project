package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.model.MemberLog

interface MemberZoneLogRepository {

    suspend fun save(log: MemberLog): MemberLog

}