package com.kona.ktca.domain.dto

import com.kona.common.infrastructure.enumerate.MemberRole
import com.kona.common.infrastructure.enumerate.MemberStatus
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import java.time.LocalDateTime

class MemberDTOFixture {

    fun giveOne(
        memberId: Long? = null,
        loginId: String = SnowflakeIdGenerator.generate()
    ): MemberDTO {
        return MemberDTO(
            memberId = memberId,
            loginId = loginId,
            password = "${loginId}-password",
            name = "${loginId}-name",
            email = "${loginId}-email",
            team = "${loginId}-team",
            role = MemberRole.VIEWER,
            status = MemberStatus.ACTIVE,
            lastLoginAt = LocalDateTime.now()
        )
    }

}
