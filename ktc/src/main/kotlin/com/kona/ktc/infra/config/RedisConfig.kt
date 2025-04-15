package com.kona.ktc.infra.config

import io.lettuce.core.resource.DefaultClientResources
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisSentinelConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties
) {

    @Bean
    fun numberRedisTemplate(): RedisTemplate<String, Number> {
        return RedisTemplate<String, Number>().apply {
            this.keySerializer = StringRedisSerializer()
            this.valueSerializer = GenericJackson2JsonRedisSerializer()
            this.connectionFactory = redisConnectionFactory()
        }
    }

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val poolConfig = GenericObjectPoolConfig<Any>().apply {
            maxTotal = 10 // 최대 커넥션 개수 설정
            maxIdle = 5   // 최대 유휴 커넥션 개수 설정
            minIdle = 1   // 최소 유휴 커넥션 개수 설정
        }
        val clientResources = DefaultClientResources.create()
        val clientConfig = LettucePoolingClientConfiguration.builder()
            .poolConfig(poolConfig)
            .clientResources(clientResources)
            .build()
        val redisSentinelConfiguration = RedisSentinelConfiguration(
            redisProperties.sentinel.master,
            redisProperties.sentinel.nodes.toSet()
        )
        redisSentinelConfiguration.password = RedisPassword.of(redisProperties.password)

        return LettuceConnectionFactory(redisSentinelConfiguration, clientConfig)
    }

}