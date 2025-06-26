package com.kona.ktca.infrastructure.config

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.application.usecase.MemberManagementUseCase
import com.kona.ktca.application.usecase.TrafficZoneManagementUseCase
import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class KtcaDatabaseInitializer(
    private val memberManagementUseCase: MemberManagementUseCase,
    private val trafficZoneManagementUseCase: TrafficZoneManagementUseCase
) : CommandLineRunner {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun run(vararg args: String?) = runBlocking {
        logger.info("===== START Initialize default traffic zone =====")
        initDefaultTrafficZone()
        logger.info("====== END Initialize default traffic zone ======")
    }

    private suspend fun initDefaultTrafficZone() {
        saveTrafficZone(generateZone(zoneId = "KZ0000000000000000000", zoneAlias = "WARM_UP ZONE"))
        saveTrafficZone(generateZone(zoneId = "000140000000000:APP_MAIN", zoneAlias = "경기 ASP 메인 화면"))
        saveTrafficZone(generateZone(zoneId = "000140000000000:APP_RECHARGE", zoneAlias = "경기 ASP 충전 화면"))
    }

    private suspend fun generateZone(zoneId: String, zoneAlias: String): TrafficZoneDTO {
        val defaultThreshold = 1000L
        val defaultGroupId = "KG0000000000000000000"
        val activationTime = "20250601000000".convertPatternOf()
        val memberId = memberManagementUseCase.findMember(dto = MemberDTO(loginId = "admin")).memberId

        return TrafficZoneDTO(
            zoneId = zoneId,
            zoneAlias = zoneAlias,
            threshold = defaultThreshold,
            groupId = defaultGroupId,
            activationTime = activationTime,
            requesterId = memberId
        )
    }

    private suspend fun saveTrafficZone(dto: TrafficZoneDTO) {
        try {
            trafficZoneManagementUseCase.createTrafficZone(dto)
            logger.info("Created ZONE : ${dto.zoneId}(${dto.zoneAlias})")
        } catch (e: Exception) {
            logger.error("Failed Initialize default traffic zone. [zoneId: ${dto.zoneId}]", e)
        }
    }

}