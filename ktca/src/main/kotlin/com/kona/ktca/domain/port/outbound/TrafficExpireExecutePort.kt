package com.kona.ktca.domain.port.outbound

interface TrafficExpireExecutePort {

    suspend fun execute(): Long

}