package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.MemberRole
import com.kona.common.infrastructure.enumerate.MemberStatus
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import java.time.LocalDateTime

class MemberFixture {

    fun giveOne(loginId: String = SnowflakeIdGenerator.generate()): Member {
        return Member(
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
