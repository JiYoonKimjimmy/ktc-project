package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.*
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.outbound.TrafficZoneSavePort
import com.kona.ktca.infrastructure.repository.TrafficZoneRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.time.ZoneOffset

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
        return with(trafficZone) {
            saveCache(trafficZone = this)
            saveEntity(trafficZone = this)
        }
    }

    private suspend fun saveCache(trafficZone: TrafficZone) {
        val zoneId = trafficZone.zoneId
        val threshold = trafficZone.threshold.toString()
        val zoneStatus = mapOf(
            "status" to trafficZone.status.name,
            "activationTime" to trafficZone.activationTime.toInstant(ZoneOffset.UTC).toEpochMilli().toString()
        )

        redisExecuteAdapter.setValue(THRESHOLD.getKey(zoneId), threshold)
        redisExecuteAdapter.pushHashMap(QUEUE_STATUS.getKey(zoneId), zoneStatus)

        when (trafficZone.status) {
            ACTIVE -> redisExecuteAdapter.addValueForSet(ACTIVATION_ZONES.key, zoneId)
            BLOCKED, DELETED -> redisExecuteAdapter.removeValueForSet(ACTIVATION_ZONES.key, zoneId)
        }
    }

    private suspend fun saveEntity(trafficZone: TrafficZone): TrafficZone = withContext(Dispatchers.IO) {
        trafficZoneRepository.save(TrafficZoneEntity(trafficZone)).toDomain()
    }

}