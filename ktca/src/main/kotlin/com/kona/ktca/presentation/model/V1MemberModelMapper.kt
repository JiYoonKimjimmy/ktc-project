package com.kona.ktca.presentation.model

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.dto.MemberLogType
import com.kona.ktca.dto.MemberRole
import com.kona.ktca.dto.MemberStatus
import com.kona.ktca.dto.V1MemberData
import com.kona.ktca.dto.V1MemberLogData
import com.kona.ktca.dto.V1MemberZoneLogData
import com.kona.ktca.dto.ZoneStatus
import org.springframework.stereotype.Component

@Component
class V1MemberModelMapper {

    fun domainToModel(member: Member): V1MemberData {
        return V1MemberData(
            memberId = member.memberId,
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

    fun domainToModel(log: MemberLog): V1MemberLogData {
        return V1MemberLogData(
            memberId = log.member.memberId,
            loginId = log.member.loginId,
            name = log.member.name,
            zoneLog = V1MemberZoneLogData(
                logType = MemberLogType.entries.find { it.value == log.type.name },
                zoneId = log.zoneLog.zoneId,
                zoneAlias = log.zoneLog.zoneAlias,
                threshold = log.zoneLog.threshold.toInt(),
                zoneStatus = ZoneStatus.valueOf(log.zoneLog.status.name),
                activationTime = log.zoneLog.activationTime.convertPatternOf(),
                created = log.zoneLog.created?.convertPatternOf(),
                updated = log.zoneLog.updated?.convertPatternOf(),
            ),
            created = log.created?.convertPatternOf(),
            updated = log.updated?.convertPatternOf()
        )
    }

}