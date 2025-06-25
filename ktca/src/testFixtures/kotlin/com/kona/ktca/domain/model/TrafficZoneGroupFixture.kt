package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import java.util.concurrent.atomic.AtomicInteger

object TrafficZoneGroupFixture {

    private val orderGenerator = AtomicInteger(0)

    fun giveOne(
        groupId: Long? = null,
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