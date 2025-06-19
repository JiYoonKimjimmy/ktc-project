package com.kona.ktca.domain.port.outbound

interface MemberFindPort {

    suspend fun existsByLoginId(loginId: String): Boolean

}