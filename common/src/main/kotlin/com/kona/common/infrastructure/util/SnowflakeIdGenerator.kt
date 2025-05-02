package com.kona.common.infrastructure.util

import java.net.InetAddress

object SnowflakeIdGenerator {

    private const val WORKER_ID_BITS = 5L
    private const val SEQUENCE_BITS = 12L

    const val MAX_WORKER_ID = -1L xor (-1L shl WORKER_ID_BITS.toInt())
    const val MAX_SEQUENCE = -1L xor (-1L shl SEQUENCE_BITS.toInt())

    private const val WORKER_ID_SHIFT = SEQUENCE_BITS
    private const val TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS

    private var workerId: Long = 0
    private var sequence = 0L
    private var lastTimestamp = -1L
    
    init {
        // 워커 ID 설정 (예: 서버 IP의 마지막 옥텟 사용)
        workerId = InetAddress.getLocalHost().hostAddress.split(".")[3].toLong() % (MAX_WORKER_ID + 1)
    }
    
    @Synchronized
    fun generate(radix: Int = RADIX_DEC): String {
        var timestamp = System.currentTimeMillis()
        
        if (timestamp < lastTimestamp) {
            throw RuntimeException("Clock moved backwards")
        }
        
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) and MAX_SEQUENCE
            if (sequence == 0L) {
                timestamp = tilNextMillis(lastTimestamp)
            }
        } else {
            sequence = 0
        }
        
        lastTimestamp = timestamp
        
        val id = ((timestamp shl TIMESTAMP_LEFT_SHIFT.toInt())
                or (workerId shl WORKER_ID_SHIFT.toInt())
                or sequence)
        
        return id.toString(radix)
    }
    
    private fun tilNextMillis(lastTimestamp: Long): Long {
        var timestamp = System.currentTimeMillis()
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis()
        }
        return timestamp
    }
}