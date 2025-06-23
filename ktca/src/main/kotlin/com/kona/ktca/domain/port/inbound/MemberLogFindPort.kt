package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.dto.MemberLogDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.MemberLog
import org.springframework.data.domain.Page

interface MemberLogFindPort {

    suspend fun findPageMemberLog(dto: MemberLogDTO, pageable: PageableDTO): Page<MemberLog>

}