package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.common.infrastructure.util.TRAFFIC_GROUP_ID_PREFIX
import java.util.concurrent.atomic.AtomicInteger

object TrafficZoneGroupFixture {

    private val orderGenerator = AtomicInteger(0)

    fun giveOne(
        groupId: String = "$TRAFFIC_GROUP_ID_PREFIX${SnowflakeIdGenerator.generate()}",
        name: String = "그룹-${SnowflakeIdGenerator.generate()}",
        order: Int? = null,
        status: TrafficZoneGroupStatus = TrafficZoneGroupStatus.ACTIVE,
    ): TrafficZoneGroup {
        val groupOrder = if (order == null || order <= orderGenerator.get()) {
            orderGenerator.incrementAndGet()
        } else {
            order
        }
        return TrafficZoneGroup(
            groupId = groupId,
            name = name,
            order = groupOrder,
            status = status
        )
    }

}