package com.kona.common.infrastructure.config

import jakarta.annotation.PostConstruct
import org.redisson.api.RedissonClient
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@Configuration
class RedisConfig(
    private val environment: Environment,
    private val redisProperties: RedisProperties
) {

    lateinit var activeProfile: String

    @PostConstruct
    fun initialize() {
        val profiles = listOf("test", "dev", "qa", "prod")
        this.activeProfile = environment.activeProfiles.find { profiles.contains(it) } ?: "test"
    }

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        return LettuceConfig(activeProfile, redisProperties).lettuceConnectionFactory()
    }

    @Bean
    fun redissonClient(): RedissonClient {
        return RedissonConfig(activeProfile, redisProperties).redissonClient()
    }

}