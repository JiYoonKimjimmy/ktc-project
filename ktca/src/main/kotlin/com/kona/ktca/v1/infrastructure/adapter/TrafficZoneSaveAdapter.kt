package com.kona.ktca.v1.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE_STATUS
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.THRESHOLD
import com.kona.ktca.v1.domain.model.TrafficZone
import com.kona.ktca.v1.domain.port.outbound.TrafficZoneSavePort
import com.kona.ktca.v1.infrastructure.repository.TrafficZoneRepository
import com.kona.ktca.v1.infrastructure.repository.entity.TrafficZoneEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class TrafficZoneSaveAdapter(
    private val trafficZoneRepository: TrafficZoneRepository,
    private val redisExecuteAdapter: RedisExecuteAdapter,
) : TrafficZoneSavePort {

    override suspend fun save(trafficZone: TrafficZone): TrafficZone {
        /**
         * [트래픽 Zone 설정 정보 저장]
         * 1. 트래픽 Zone 정보 Cache 저장
         * 2. 트래픽 Zone 정보 DB 저장
         */
        saveCache(trafficZone)
        return saveEntity(trafficZone)
    }

    private suspend fun saveCache(trafficZone: TrafficZone) {
        val zoneId = trafficZone.zoneId
        redisExecuteAdapter.setValue(THRESHOLD.getKey(zoneId), trafficZone.threshold.toString())
        redisExecuteAdapter.setValue(QUEUE_STATUS.getKey(zoneId), trafficZone.status.name)
    }

    private suspend fun saveEntity(trafficZone: TrafficZone): TrafficZone = withContext(Dispatchers.IO) {
        trafficZoneRepository.save(TrafficZoneEntity(trafficZone)).toDomain()
    }

}