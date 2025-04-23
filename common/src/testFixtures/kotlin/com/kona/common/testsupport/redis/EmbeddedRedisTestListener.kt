package com.kona.common.testsupport.redis

import io.kotest.core.listeners.ProjectListener
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import org.springframework.data.redis.core.RedisTemplate

class EmbeddedRedisTestListener(
    redisTemplate: RedisTemplate<*, *>? = null,
) : ProjectListener, TestListener {

    private val template = redisTemplate ?: EmbeddedRedis.stringRedisTemplate

    override suspend fun beforeProject() {
        EmbeddedRedis.start()
    }

    override suspend fun afterProject() {
        EmbeddedRedis.stop()
    }

    override suspend fun beforeSpec(spec: Spec) {
        template.execute { connection -> connection.serverCommands().flushAll() }
    }

    override suspend fun afterSpec(spec: Spec) {
        template.execute { connection -> connection.close() }
    }

}