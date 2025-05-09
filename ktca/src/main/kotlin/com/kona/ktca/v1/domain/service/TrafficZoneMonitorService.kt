package com.kona.ktca.v1.domain.service

import com.kona.ktca.v1.domain.model.TrafficMonitoring
import com.kona.ktca.v1.domain.port.inbound.TrafficZoneMonitorPort
import org.springframework.stereotype.Service

@Service
class TrafficZoneMonitorService : TrafficZoneMonitorPort {

    override fun monitoring(zoneId: String): TrafficMonitoring {
        /**
         * [트래픽 Zone 제어 현황 모니터링]
         * 1. 트래픽 제어 활성화 상태 Zone 목록 조회
         * 2. 각 Zone 별 트래픽 현황 조회
         *    - Zone 대기자 수
         *    - Zone 진입자 수
         *    - Zone 대기 해소 예상 시간
         */
        TODO("Not yet implemented")
    }

}