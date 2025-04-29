package com.kona.common.infrastructure.redis

import org.springframework.data.redis.core.script.RedisScript

interface RedisScriptExecuteAdapter {

    suspend fun execute(script: RedisScript<List<*>>, keys: List<String>, args: List<String>): List<*>

}