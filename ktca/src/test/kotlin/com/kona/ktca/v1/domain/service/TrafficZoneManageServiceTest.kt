package com.kona.ktca.v1.domain.service

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.*
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.util.TRAFFIC_ZONE_ID_PREFIX
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktca.v1.domain.model.TrafficZone
import com.kona.ktca.v1.domain.port.dto.TrafficZoneDTO
import com.kona.ktca.v1.infrastructure.adapter.TrafficZoneFindAdapter
import com.kona.ktca.v1.infrastructure.adapter.TrafficZoneSaveAdapter
import com.kona.ktca.v1.infrastructure.repository.FakeTrafficZoneRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveMaxLength
import io.kotest.matchers.string.shouldStartWith
import java.time.LocalDateTime

class TrafficZoneManageServiceTest : BehaviorSpec({

    val trafficZoneRepository = FakeTrafficZoneRepository()
    val redisExecuteAdapter = RedisExecuteAdapterImpl(EmbeddedRedis.reactiveStringRedisTemplate)
    val trafficZoneSaveAdapter = TrafficZoneSaveAdapter(trafficZoneRepository, redisExecuteAdapter)
    val trafficZoneFindAdapter = TrafficZoneFindAdapter(trafficZoneRepository)
    val trafficZoneManageService = TrafficZoneManageService(trafficZoneSaveAdapter, trafficZoneFindAdapter)

    lateinit var saved: TrafficZone

    given("트래픽 Zone 저장 요청되어") {
        val newTrafficZone = TrafficZoneDTO(
            zoneAlias = "test-zone-alias",
            threshold = 1000,
            activationTime = LocalDateTime.now(),
            status = ACTIVE
        )

        `when`("신규 등록 요청인 경우") {
            saved = trafficZoneManageService.save(newTrafficZone)

            then("DB 저장 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(saved.zoneId)
                entity!! shouldNotBe null
                entity.id shouldBe saved.zoneId
                entity.alias shouldBe saved.zoneAlias
                entity.threshold shouldBe saved.threshold
                entity.activationTime shouldBe saved.activationTime
                entity.status shouldBe saved.status
            }

            then("트래픽 Zone 'threshold: 1000' Cache 저장 결과 정상 확인한다") {
                val threshold = redisExecuteAdapter.getValue(TrafficCacheKey.THRESHOLD.getKey(saved.zoneId))
                threshold shouldBe "1000"
            }

            then("트래픽 Zone 'status: ACTIVE' Cache 저장 결과 정상 확인한다") {
                val status = redisExecuteAdapter.getValue(TrafficCacheKey.QUEUE_STATUS.getKey(saved.zoneId))
                status shouldBe ACTIVE.name
            }

            then("신규 등록 트래픽 Zone 'zoneId' 채번 규칙 정상 확인한다") {
                val zoneId = saved.zoneId
                zoneId shouldStartWith TRAFFIC_ZONE_ID_PREFIX
                zoneId shouldHaveMaxLength 21
            }

            then("신규 등록 트래픽 Zone 'ACTIVATION_ZONES' Cache 저장 정보 정상 확인한다") {
                val zones = redisExecuteAdapter.getValuesForSet(TrafficCacheKey.ACTIVATION_ZONES.key)
                zones.contains(saved.zoneId) shouldBe true
            }
        }

        val updateZoneAlias = "test-zone-alias-updated"
        val updateZoneAliasTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            zoneAlias = updateZoneAlias
        )

        `when`("트래픽 Zone 'zoneAlias' 정보 변경 요청인 경우") {
            val result = trafficZoneManageService.save(updateZoneAliasTrafficZone)

            then("DB 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.id shouldBe result.zoneId
                entity.alias shouldBe updateZoneAlias
            }
        }

        val updateThreshold = 2000L
        val updateThresholdTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            threshold = updateThreshold
        )

        `when`("트래픽 Zone 'threshold' 정보 변경 요청인 경우") {
            val result = trafficZoneManageService.save(updateThresholdTrafficZone)

            then("DB 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.id shouldBe result.zoneId
                entity.alias shouldBe updateZoneAlias
                entity.threshold shouldBe updateThreshold
            }

            then("트래픽 Zone 'threshold: 2000' Cache 변경 결과 정상 확인한다") {
                val threshold = redisExecuteAdapter.getValue(TrafficCacheKey.THRESHOLD.getKey(result.zoneId))
                threshold shouldBe "2000"
            }
        }

        val updateStatus = BLOCKED
        val updateStatusTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            status = updateStatus
        )

        `when`("트래픽 Zone 'status' 정보 변경 요청인 경우") {
            val result = trafficZoneManageService.save(updateStatusTrafficZone)

            then("DB 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.id shouldBe result.zoneId
                entity.alias shouldBe updateZoneAlias
                entity.threshold shouldBe updateThreshold
                entity.status shouldBe updateStatus
            }

            then("트래픽 Zone 'status: BLOCKED' Cache 변경 결과 정상 확인한다") {
                val status = redisExecuteAdapter.getValue(TrafficCacheKey.QUEUE_STATUS.getKey(result.zoneId))
                status shouldBe "BLOCKED"
            }

            then("트래픽 Zone 'status: BLOCKED' 되어 'ACTIVATION_ZONES' Cache 삭제 정상 확인한다") {
                val zones = redisExecuteAdapter.getValuesForSet(TrafficCacheKey.ACTIVATION_ZONES.key)
                zones.contains(saved.zoneId) shouldBe false
            }
        }

        val updateActivationTime = LocalDateTime.now().plusDays(1)
        val updateActivationTimeTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            activationTime = updateActivationTime
        )

        `when`("트래픽 Zone 'activationTime' 정보 변경 요청인 경우") {
            val result = trafficZoneManageService.save(updateActivationTimeTrafficZone)

            then("DB 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.id shouldBe result.zoneId
                entity.alias shouldBe updateZoneAlias
                entity.threshold shouldBe updateThreshold
                entity.status shouldBe updateStatus
                entity.activationTime shouldBe updateActivationTime
            }
        }

        val deleteStatusTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            status = DELETED
        )

        `when`("트래픽 Zone 'status' 정보 변경 요청인 경우") {
            val result = trafficZoneManageService.save(deleteStatusTrafficZone)

            then("DB 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.status shouldBe DELETED
            }

            then("트래픽 Zone 'status: DELETED' Cache 변경 결과 정상 확인한다") {
                val threshold = redisExecuteAdapter.getValue(TrafficCacheKey.QUEUE_STATUS.getKey(result.zoneId))
                threshold shouldBe "DELETED"
            }

            then("트래픽 Zone 'status: DELETED' 되어 'ACTIVATION_ZONES' Cache 삭제 정상 확인한다") {
                val zones = redisExecuteAdapter.getValuesForSet(TrafficCacheKey.ACTIVATION_ZONES.key)
                zones.contains(saved.zoneId) shouldBe false
            }
        }

        val errorUpdateStatusTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            status = ACTIVE
        )

        `when`("트래픽 Zone 'status' 정보 변경 요청인 경우") {
            val result = shouldThrow<InternalServiceException> { trafficZoneManageService.save(errorUpdateStatusTrafficZone) }

            then("'DELETED_TRAFFIC_ZONE_STATUS_NOT_CHANGED' 예외 발생 정상 확인한다") {
                result.errorCode shouldBe ErrorCode.DELETED_TRAFFIC_ZONE_STATUS_NOT_CHANGED
            }
        }
    }

})