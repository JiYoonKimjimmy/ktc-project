package com.kona.common.infrastructure.redis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component

@Component
class RedisScriptExecuteAdapterImpl(
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,
) : RedisScriptExecuteAdapter {

    override suspend fun execute(script: RedisScript<List<*>>, keys: List<String>, args: List<String>): List<*> = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.execute(script, keys, *args.toTypedArray()).awaitSingle()
    }

}