package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.model.Member

interface MemberSavePort {

    suspend fun save(member: Member): Member

}