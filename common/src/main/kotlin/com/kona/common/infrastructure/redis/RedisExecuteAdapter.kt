package com.kona.common.infrastructure.redis

import org.springframework.data.redis.core.script.RedisScript

interface RedisExecuteAdapter {

    suspend fun execute(script: RedisScript<List<*>>, keys: List<String>, args: List<String>): List<*>

    suspend fun keys(pattern: String): List<String>

    suspend fun getValue(key: String): String?

    suspend fun getZSetSize(key: String): Long

}