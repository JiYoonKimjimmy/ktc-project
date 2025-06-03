package com.kona.ktc.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scripting.support.ResourceScriptSource

@EnableScheduling
@ComponentScan(basePackages = ["com.kona.ktc", "com.kona.common"])
@Configuration
class KtcApplicationConfig {

    @Bean
    fun trafficControlScript(): RedisScript<List<*>> {
        return DefaultRedisScript(
            ResourceScriptSource(ClassPathResource("scripts/traffic-control.lua")).scriptAsString,
            List::class.java
        )
    }

}