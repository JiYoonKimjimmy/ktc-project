package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.dto.MemberLogDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.MemberLog
import org.springframework.data.domain.Page

interface MemberZoneLogRepository {

    suspend fun save(log: MemberLog): MemberLog

    suspend fun findPageByPredicate(dto: MemberLogDTO, pageable: PageableDTO): Page<MemberLog>

}