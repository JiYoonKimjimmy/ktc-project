package com.kona.ktca.domain.service

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.*
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.util.QUEUE_ACTIVATION_TIME_KEY
import com.kona.common.infrastructure.util.QUEUE_STATUS_KEY
import com.kona.common.infrastructure.util.TRAFFIC_ZONE_ID_PREFIX
import com.kona.common.infrastructure.util.convertUTCEpochTime
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.dto.TrafficZoneDTOFixture
import com.kona.ktca.domain.model.TrafficZoneFixture
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.model.TrafficZoneGroupFixture
import com.kona.ktca.infrastructure.adapter.TrafficZoneCachingAdapter
import com.kona.ktca.infrastructure.repository.FakeTrafficZoneGroupRepository
import com.kona.ktca.infrastructure.repository.FakeTrafficZoneRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveMaxLength
import io.kotest.matchers.string.shouldStartWith
import java.time.LocalDateTime

class TrafficZoneSaveServiceTest : BehaviorSpec({

    val redisExecuteAdapter = RedisExecuteAdapterImpl(EmbeddedRedis.reactiveStringRedisTemplate)
    val trafficZoneCachingPort = TrafficZoneCachingAdapter(redisExecuteAdapter)
    val trafficZoneRepository = FakeTrafficZoneRepository()
    val trafficZoneGroupRepository = FakeTrafficZoneGroupRepository()
    val trafficZoneSaveService = TrafficZoneSaveService(trafficZoneRepository, trafficZoneCachingPort)

    lateinit var savedGroup1: TrafficZoneGroup
    lateinit var savedGroup2: TrafficZoneGroup

    beforeSpec {
        savedGroup1 = trafficZoneGroupRepository.save(group = TrafficZoneGroupFixture.giveOne())
        savedGroup2 = trafficZoneGroupRepository.save(group = TrafficZoneGroupFixture.giveOne())
    }

    given("트래픽 Zone 정보 신규 등록 요청되어") {
        val newTrafficZone = TrafficZoneDTOFixture.giveOne(
            zoneAlias = "test-zone-alias",
            threshold = 1000,
            groupId = savedGroup1.groupId,
            status = ACTIVE,
            activationTime = LocalDateTime.now(),
            group = savedGroup1
        )

        `when`("정상 요청인 경우") {
            val result = trafficZoneSaveService.create(newTrafficZone)

            then("DB 저장 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.zoneId shouldBe result.zoneId
                entity.zoneAlias shouldBe result.zoneAlias
                entity.threshold shouldBe result.threshold
                entity.groupId shouldBe result.groupId
                entity.status shouldBe result.status
                entity.activationTime shouldBe result.activationTime
            }

            then("트래픽 Zone 'threshold: 1000' Cache 저장 결과 정상 확인한다") {
                val threshold = redisExecuteAdapter.getValue(THRESHOLD.getKey(result.zoneId))
                threshold shouldBe "1000"
            }

            then("트래픽 Zone 'status: ACTIVE' Cache 저장 결과 정상 확인한다") {
                val status = redisExecuteAdapter.getHashValue(QUEUE_STATUS.getKey(result.zoneId), QUEUE_STATUS_KEY)
                status shouldBe ACTIVE.name
            }

            then("트래픽 Zone 'activationTime' Cache 저장 결과 정상 확인한다") {
                val status = redisExecuteAdapter.getHashValue(QUEUE_STATUS.getKey(result.zoneId), QUEUE_ACTIVATION_TIME_KEY)
                status shouldBe newTrafficZone.activationTime?.convertUTCEpochTime()
            }

            then("신규 등록 트래픽 Zone 'zoneId' 채번 규칙 정상 확인한다") {
                val zoneId = result.zoneId
                zoneId shouldStartWith TRAFFIC_ZONE_ID_PREFIX
                zoneId shouldHaveMaxLength 21
            }

            then("신규 등록 트래픽 Zone 'ACTIVATION_ZONES' Cache 저장 정보 정상 확인한다") {
                val zones = redisExecuteAdapter.getValuesForSet(ACTIVATION_ZONES.key)
                zones.contains(result.zoneId) shouldBe true
            }
        }
    }

    given("트래픽 Zone 정보 변경 요청 되어") {
        val newTrafficZone = TrafficZoneDTOFixture.giveOne(
            zoneAlias = "test-zone-alias",
            threshold = 1000,
            groupId = savedGroup1.groupId,
            status = ACTIVE,
            activationTime = LocalDateTime.now(),
            group = savedGroup1
        )
        val saved = trafficZoneSaveService.create(newTrafficZone)

        val thresholdKey = THRESHOLD.getKey(saved.zoneId)
        val queueStatusKey = QUEUE_STATUS.getKey(saved.zoneId)
        val activationZonesKey = ACTIVATION_ZONES.key

        val updateZoneAlias = "test-zone-alias-updated"
        val updateZoneAliasTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            zoneAlias = updateZoneAlias
        )

        `when`("트래픽 Zone 'zoneAlias' 정보 변경 요청인 경우") {
            val result = trafficZoneSaveService.update(saved, updateZoneAliasTrafficZone)

            then("DB 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.zoneId shouldBe result.zoneId
                entity.zoneAlias shouldBe updateZoneAlias
            }
        }

        val updateThreshold = 2000L
        val updateThresholdTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            threshold = updateThreshold
        )

        `when`("트래픽 Zone 'threshold' 정보 변경 요청인 경우") {
            val result = trafficZoneSaveService.update(saved, updateThresholdTrafficZone)

            then("DB 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.zoneId shouldBe result.zoneId
                entity.threshold shouldBe updateThreshold
            }

            then("트래픽 Zone 'threshold: 2000' Cache 변경 결과 정상 확인한다") {
                val threshold = redisExecuteAdapter.getValue(thresholdKey)
                threshold shouldBe "2000"
            }
        }

        val updateGroupTrafficZone = TrafficZoneDTO(zoneId = saved.zoneId).applyGroup(savedGroup2)

        `when`("트래픽 Zone 'groupId' 변경 요청인 경우") {
            val result = trafficZoneSaveService.update(saved, updateGroupTrafficZone)

            then("DB 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.zoneId shouldBe result.zoneId
                entity.groupId shouldBe savedGroup2.groupId
            }
        }

        val updateStatus = BLOCKED
        val updateStatusTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            status = updateStatus
        )

        `when`("트래픽 Zone 'status' 정보 변경 요청인 경우") {
            val result = trafficZoneSaveService.update(saved, updateStatusTrafficZone)

            then("DB 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.zoneId shouldBe result.zoneId
                entity.status shouldBe updateStatus
            }

            then("트래픽 Zone 'status: BLOCKED' Cache 변경 결과 정상 확인한다") {
                val status = redisExecuteAdapter.getHashValue(queueStatusKey, QUEUE_STATUS_KEY)
                status shouldBe BLOCKED.name
            }

            then("트래픽 Zone 'status: BLOCKED' 되어 'ACTIVATION_ZONES' Cache 삭제 정상 확인한다") {
                val zones = redisExecuteAdapter.getValuesForSet(activationZonesKey)
                zones.contains(saved.zoneId) shouldBe false
            }
        }

        val updateActivationTime = LocalDateTime.now().plusDays(1)
        val updateActivationTimeTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            activationTime = updateActivationTime
        )

        `when`("트래픽 Zone 'activationTime' 정보 변경 요청인 경우") {
            val result = trafficZoneSaveService.update(saved, updateActivationTimeTrafficZone)

            then("DB 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.zoneId shouldBe result.zoneId
                entity.activationTime shouldBe updateActivationTime
            }

            then("트래픽 Zone 'activationTime' Cache 변경 결과 정상 확인한다") {
                val activationTime = redisExecuteAdapter.getHashValue(queueStatusKey, QUEUE_ACTIVATION_TIME_KEY)
                activationTime shouldBe updateActivationTime.convertUTCEpochTime()
            }
        }

        val deleteStatusTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            status = DELETED
        )

        `when`("트래픽 Zone 'status' 정보 'ACTIVE > DELETED' 변경 요청인 경우") {
            val result = trafficZoneSaveService.update(saved, deleteStatusTrafficZone)

            then("DB 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(result.zoneId)
                entity!! shouldNotBe null
                entity.status shouldBe DELETED
            }

            then("트래픽 Zone 'status: DELETED' Cache 변경 결과 정상 확인한다") {
                val status = redisExecuteAdapter.getHashValue(queueStatusKey, QUEUE_STATUS_KEY)
                status shouldBe "DELETED"
            }

            then("트래픽 Zone 'status: DELETED' 되어 'ACTIVATION_ZONES' Cache 삭제 정상 확인한다") {
                val zones = redisExecuteAdapter.getValuesForSet(activationZonesKey)
                zones.contains(saved.zoneId) shouldBe false
            }
        }

        val deleteTrafficZone = trafficZoneRepository.findByZoneId(zoneId = saved.zoneId)!!
        val errorUpdateStatusTrafficZone = TrafficZoneDTO(
            zoneId = saved.zoneId,
            status = ACTIVE
        )

        `when`("이미 'DELETED' 상태 변경되 트래픽 Zone 'status' 정보 변경 요청인 경우") {
            val result = shouldThrow<InternalServiceException> { trafficZoneSaveService.update(deleteTrafficZone, errorUpdateStatusTrafficZone) }

            then("'DELETED_TRAFFIC_ZONE_STATUS_NOT_CHANGED' 예외 발생 정상 확인한다") {
                result.errorCode shouldBe ErrorCode.DELETED_TRAFFIC_ZONE_CANNOT_BE_CHANGED
            }
        }
    }

    given("트래픽 Zone 정보 삭제 요청되어") {
        val newTrafficZone = TrafficZoneDTOFixture.giveOne(
            zoneAlias = "test-zone-alias",
            threshold = 1000,
            groupId = savedGroup1.groupId,
            status = ACTIVE,
            activationTime = LocalDateTime.now(),
            group = savedGroup1
        )
        val saved = trafficZoneSaveService.create(newTrafficZone)

        `when`("트래픽 Zone 삭제 처리 성공인 경우") {
            trafficZoneSaveService.delete(saved.zoneId)

            then("'status' DB 정보 'ACTIVE > DELETED' 변경 결과 정상 확인한다") {
                val entity = trafficZoneRepository.findByZoneId(saved.zoneId)
                entity!! shouldNotBe null
                entity.status shouldBe DELETED
                entity.group shouldBe null
            }

            then("'ACTIVATION_ZONES' Cache 목록 제거 결과 정상 확인한다") {
                val zones = redisExecuteAdapter.getValuesForSet(ACTIVATION_ZONES.key)
                zones.contains(saved.zoneId) shouldBe false
            }

            then("삭제 'zoneId' 기준 모든 Cache 삭제 결과 정상 확인다") {
                TrafficCacheKey.getTrafficControlKeys(saved.zoneId).values.forEach {
                    redisExecuteAdapter.hasKey(it) shouldBe false
                }
            }
        }
    }

})