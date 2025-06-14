package com.kona.common.infrastructure.cache.redis

import io.lettuce.core.RestoreArgs.Builder.ttl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.data.redis.core.*
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

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

    override suspend fun getValue(key: String): String? = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.opsForValue().getAndAwait(key)
    }

    override suspend fun setValue(key: String, value: String): Boolean = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.opsForValue().setAndAwait(key, value)
    }

    override suspend fun getSizeForZSet(key: String): Long = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.opsForZSet().sizeAndAwait(key)
    }

    override suspend fun addValueForSet(key: String, value: String): Long = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.opsForSet().addAndAwait(key, value)
    }

    override suspend fun removeValueForSet(key: String, value: String): Long = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.opsForSet().removeAndAwait(key, value)
    }

    override suspend fun getValuesForSet(key: String): List<String> = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.opsForSet().members(key).collectList().awaitSingle()
    }

    override suspend fun hasKey(key: String): Boolean = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.hasKeyAndAwait(key)
    }

    override suspend fun deleteAll(keys: List<String>): Long = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.deleteAndAwait(*keys.toTypedArray())
    }

    override suspend fun pushHashMap(key: String, args: Map<String, String>): Boolean = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.opsForHash<String, String>().putAllAndAwait(key, args)
    }

    override suspend fun getHashValue(key: String, hashKey: String): String? = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.opsForHash<String, String>().getAndAwait(key, hashKey)
    }

    override suspend fun expire(key: String, duration: Duration): Boolean = withContext(Dispatchers.IO) {
        reactiveStringRedisTemplate.expireAndAwait(key, duration)
    }

}