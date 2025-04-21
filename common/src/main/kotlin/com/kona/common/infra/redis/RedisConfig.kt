package com.kona.common.infra.redis

import io.lettuce.core.resource.DefaultClientResources
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisPassword
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
            maxTotal = 10
            maxIdle = 5
            minIdle = 1
        }
        val clientResources = DefaultClientResources.create()
        val clientConfig = LettucePoolingClientConfiguration.builder()
            .poolConfig(poolConfig)
            .clientResources(clientResources)
            .build()

        val redisClusterConfiguration = RedisClusterConfiguration(redisProperties.cluster.nodes)
        redisClusterConfiguration.password = RedisPassword.of(redisProperties.password)

        return LettuceConnectionFactory(redisClusterConfiguration, clientConfig)
    }

}