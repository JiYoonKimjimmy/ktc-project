package com.kona.ktca.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.scripting.support.ResourceScriptSource

@EnableJpaAuditing
@EnableJpaRepositories("com.kona.ktca")
@ComponentScan("com.kona.common")
@Configuration
class KtcaApplicationConfig {

    @Bean
    fun trafficExpireScript(): RedisScript<List<*>> {
        return DefaultRedisScript(
            ResourceScriptSource(ClassPathResource("scripts/traffic-expire.lua")).scriptAsString,
            List::class.java
        )
    }

}