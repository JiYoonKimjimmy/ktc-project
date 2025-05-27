package com.kona.ktc.domain.service

import com.kona.ktc.domain.port.inbound.TrafficMonitorPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TrafficMonitorService : TrafficMonitorPort {
    private val logger = LoggerFactory.getLogger(TrafficMonitorService::class.java)


    override suspend fun registerClient(clientId: String) {
        logger.info("registerClient called: $clientId")
    }

    override suspend fun unregisterClient(clientId: String) {
        logger.info("unregisterClient called: $clientId")
    }

    override suspend fun getTrafficUpdates(clientId: String) {
        logger.info("getTrafficUpdates called: $clientId")
    }
}
