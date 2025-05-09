package com.kona.ktca.v1.domain.port.outbound

interface TrafficExpireExecutePort {

    suspend fun execute(): Long

}