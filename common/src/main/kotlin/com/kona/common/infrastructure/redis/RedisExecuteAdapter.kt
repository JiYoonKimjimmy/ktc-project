package com.kona.common.infrastructure.redis

import org.springframework.data.redis.core.script.RedisScript

interface RedisExecuteAdapter {

    suspend fun execute(script: RedisScript<List<*>>, keys: List<String>, args: List<String>): List<*>

    suspend fun keys(pattern: String): List<String>

}