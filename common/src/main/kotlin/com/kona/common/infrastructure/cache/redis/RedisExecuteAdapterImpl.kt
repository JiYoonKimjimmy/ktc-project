package com.kona.common.infrastructure.cache.redis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.data.redis.core.sizeAndAwait
import org.springframework.stereotype.Component

@Component
class RedisExecuteAdapterImpl(
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,
) : RedisExecuteAdapter {

    override suspend fun execute(
        script: RedisScript<List<*>>,
        keys: List<String>,
        args: List<String>
    ): List<*> = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.execute(script, keys, *args.toTypedArray()).awaitSingle()
    }

    override suspend fun keys(pattern: String): List<String> = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.keys(pattern).collectList().awaitSingle()
    }

    override suspend fun getValue(key: String): String? {
        return reactiveStringRedisTemplate.opsForValue().getAndAwait(key)
    }

    override suspend fun getZSetSize(key: String): Long {
        return reactiveStringRedisTemplate.opsForZSet().sizeAndAwait(key)
    }
}