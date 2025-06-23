package com.kona.ktca.presentation.model

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.dto.V1ZoneGroupData
import com.kona.ktca.dto.ZoneGroupStatus
import org.springframework.stereotype.Component

@Component
class V1ZoneGroupModelMapper {

    fun domainToModel(group: TrafficZoneGroup): V1ZoneGroupData {
        return V1ZoneGroupData(
            groupId = group.groupId,
            groupName = group.name,
            groupOrder = group.order,
            groupStatus = ZoneGroupStatus.valueOf(group.status.name),
            created = group.created?.convertPatternOf(),
            updated = group.updated?.convertPatternOf()
        )
    }

}