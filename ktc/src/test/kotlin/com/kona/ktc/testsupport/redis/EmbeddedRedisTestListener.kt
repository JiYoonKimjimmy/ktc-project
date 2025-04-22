package com.kona.ktc.testsupport.redis

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec

class EmbeddedRedisTestListener : TestListener {

    override suspend fun beforeSpec(spec: Spec) {
        EmbeddedRedis.stringRedisTemplate.execute { connection -> connection.serverCommands().flushAll() }
    }

    override suspend fun afterSpec(spec: Spec) {
        EmbeddedRedis.stringRedisTemplate.execute { connection -> connection.close() }
    }

}