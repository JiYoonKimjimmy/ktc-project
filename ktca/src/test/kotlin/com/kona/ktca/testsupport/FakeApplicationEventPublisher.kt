package com.kona.ktca.testsupport

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.testsupport.coroutine.TestCoroutineScope
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktca.domain.event.ExpireTrafficZoneEntryCountEvent
import com.kona.ktca.domain.service.TrafficZoneEntryCountExpireService
import kotlinx.coroutines.launch
import org.springframework.context.ApplicationEventPublisher

class FakeApplicationEventPublisher : ApplicationEventPublisher {

    private val defaultCoroutineScope = TestCoroutineScope.defaultCoroutineScope
    private val redisExecuteAdapter = RedisExecuteAdapterImpl(EmbeddedRedis.reactiveStringRedisTemplate)
    private val trafficZoneEntryCountExpireService = TrafficZoneEntryCountExpireService(redisExecuteAdapter)

    override fun publishEvent(event: Any) {
        defaultCoroutineScope.launch {
            when (event) {
                is ExpireTrafficZoneEntryCountEvent -> trafficZoneEntryCountExpireService.expireTrafficZoneEntryCount(event.zoneIds)
            }
        }
    }

}