package com.kona.ktca.domain.service

import com.kona.ktca.domain.port.inbound.TrafficTokenExpirePort
import com.kona.ktca.domain.port.outbound.TrafficTokenExpireExecutePort
import org.springframework.stereotype.Service

@Service
class TrafficTokenExpireService(
    private val trafficExpireScriptExecuteAdapter: TrafficTokenExpireExecutePort,
) : TrafficTokenExpirePort {

    override suspend fun expireTraffic(): Long {
        return trafficExpireScriptExecuteAdapter.expireTrafficToken()
    }

}