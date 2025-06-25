package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.MemberRole
import com.kona.common.infrastructure.enumerate.MemberStatus
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import java.time.LocalDateTime

object MemberFixture {

    fun giveOne(
        loginId: String = SnowflakeIdGenerator.generate(),
        team: String = "${loginId}-team"
    ): Member {
        return Member(
            loginId = loginId,
            password = "${loginId}-password",
            name = "${loginId}-name",
            email = "${loginId}-email",
            team = team,
            role = MemberRole.VIEWER,
            status = MemberStatus.ACTIVE,
            lastLoginAt = LocalDateTime.now()
        )
    }

}
