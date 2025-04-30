package com.kona.ktca.v1.infrastructure.adapter.redis

import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.scripting.support.ResourceScriptSource
import org.springframework.stereotype.Component

@Component
class TrafficExpireScript {
    private lateinit var script: RedisScript<List<*>>

    @PostConstruct
    fun init() {
        script = DefaultRedisScript(
            ResourceScriptSource(ClassPathResource("scripts/traffic-expire.lua")).scriptAsString,
            List::class.java
        )
    }

    fun getScript(): RedisScript<List<*>> = script

}