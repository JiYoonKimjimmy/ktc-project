package com.kona.ktca.infrastructure.repository.entity

import com.kona.common.infrastructure.enumerate.MemberRole
import com.kona.common.infrastructure.enumerate.MemberStatus
import java.time.LocalDateTime

class MemberEntityFixture {

    fun giveOne(loginId: String): MemberEntity {
        return MemberEntity(
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