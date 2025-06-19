package com.kona.ktca.domain.dto

import com.kona.common.infrastructure.enumerate.MemberRole
import com.kona.common.infrastructure.enumerate.MemberStatus
import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import java.time.LocalDateTime

class MemberDTOFixture {

    fun giveOne(loginId: String): MemberDTO {
        return MemberDTO(
            loginId = loginId,
            password = "${loginId}-password",
            name = "${loginId}-name",
            email = "${loginId}-email",
            team = "${loginId}-team",
            role = MemberRole.USER,
            status = MemberStatus.ACTIVE,
            lastLoginAt = LocalDateTime.now()
        )
    }

}
