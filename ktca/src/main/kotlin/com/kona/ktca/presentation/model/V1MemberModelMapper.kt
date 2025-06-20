package com.kona.ktca.presentation.model

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.domain.model.Member
import com.kona.ktca.dto.MemberRole
import com.kona.ktca.dto.MemberStatus
import com.kona.ktca.dto.V1MemberData
import org.springframework.stereotype.Component

@Component
class V1MemberModelMapper {

    fun domainToModel(member: Member): V1MemberData {
        return V1MemberData(
            memberId = member.memberId?.toInt(),
            loginId = member.loginId,
            password = member.password,
            name = member.name,
            email = member.email,
            team = member.team,
            role = MemberRole.valueOf(member.role.name),
            status = MemberStatus.valueOf(member.status.name),
            lastLoginAt = member.lastLoginAt.convertPatternOf(),
            created = member.created?.convertPatternOf(),
            updated = member.updated?.convertPatternOf()
        )

    }

}