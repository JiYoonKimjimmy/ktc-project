package com.kona.common.infra.redis.mock

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisSentinelConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import redis.embedded.RedisSentinel
import redis.embedded.RedisServer

@Profile("test")
@Configuration
class RedisMockConfig(
    private val redisProperties: RedisProperties
) {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val redisServer: RedisServer by lazy {
        RedisServer
            .newRedisServer()
            .port(redisProperties.port)
            .build()
    }

    private val redisSentinel: RedisSentinel by lazy {
        RedisSentinel
            .newRedisSentinel()
            .bind(redisProperties.host)
            .masterName(redisProperties.sentinel.master)
            .build()
    }

    @PostConstruct
    fun startRedis() {
        if (!redisServer.isActive) {
            redisServer.start()
            logger.info("Embedded Redis Server Started!!")
        }

        if (!redisSentinel.isActive) {
            redisSentinel.start()
            logger.info("Embedded Redis Sentinel Started!!")
        }
    }

    @PreDestroy
    fun stopRedis() {
        redisSentinel.stop()
        redisServer.stop()
        logger.info("Embedded Redis Server Stopped!!")
    }

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisSentinelConfiguration = RedisSentinelConfiguration(
            redisProperties.sentinel.master,
            redisProperties.sentinel.nodes.toSet()
        )
        return LettuceConnectionFactory(redisSentinelConfiguration).also { it.afterPropertiesSet() }
    }

}