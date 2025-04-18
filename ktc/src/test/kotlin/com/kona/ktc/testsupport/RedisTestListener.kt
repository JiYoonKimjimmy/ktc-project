package com.kona.ktc.testsupport

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import org.springframework.data.redis.core.RedisTemplate

class RedisTestListener(
    private val redisTemplate: RedisTemplate<*, *>
) : TestListener {

    override suspend fun beforeSpec(spec: Spec) {
        redisTemplate.execute { connection -> connection.serverCommands().flushAll() }
    }

    override suspend fun afterSpec(spec: Spec) {
        redisTemplate.execute { connection -> connection.close() }
    }

}