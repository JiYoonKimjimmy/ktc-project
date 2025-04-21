package com.kona.ktc.v1.infrastructure.adapter.redis

import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets

@Component
class TrafficControlRedisScript {
    private lateinit var script: RedisScript<List<*>>

    @PostConstruct
    fun init() {
        val resource = ClassPathResource("scripts/traffic-control.lua")
        val scriptContent = StreamUtils.copyToString(resource.inputStream, StandardCharsets.UTF_8)
        script = RedisScript.of(scriptContent, List::class.java)
    }

    fun getScript(): RedisScript<List<*>> = script

} 