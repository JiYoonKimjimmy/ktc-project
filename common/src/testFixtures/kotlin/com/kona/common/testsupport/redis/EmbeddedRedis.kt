package com.kona.common.testsupport.redis

import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import redis.embedded.RedisServer

object EmbeddedRedis {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    private const val REDIS_HOST = "localhost"
    private const val REDIS_PORT = 6379

    private val redisServer: RedisServer by lazy {
        RedisServer
            .newRedisServer()
            .port(REDIS_PORT)
            .build()
    }

    private val lettuceConnectionFactory: LettuceConnectionFactory by lazy { redisConnectionFactory() }
    val stringRedisTemplate: StringRedisTemplate by lazy { stringRedisTemplate() }
    val reactiveStringRedisTemplate: ReactiveStringRedisTemplate by lazy { reactiveStringRedisTemplate() }

    fun start() {
        if (!redisServer.isActive) {
            logger.info("Embedded Redis Server Starting..")
            redisServer.start()
            logger.info("Embedded Redis Server Started!!")
        }
    }

    fun stop() {
        if (redisServer.isActive) {
            logger.info("Embedded Redis Server Stopping..")
            lettuceConnectionFactory.stop()
            redisServer.stop()
            logger.info("Embedded Redis Server Stopped!!")
        }
    }

    private fun redisConnectionFactory(): LettuceConnectionFactory {
        val redisSentinelConfiguration = RedisStandaloneConfiguration(REDIS_HOST, REDIS_PORT)
        return LettuceConnectionFactory(redisSentinelConfiguration).also { it.afterPropertiesSet() }
    }

    private fun stringRedisTemplate(): StringRedisTemplate {
        return StringRedisTemplate(lettuceConnectionFactory).also { it.afterPropertiesSet() }
    }

    private fun reactiveStringRedisTemplate(): ReactiveStringRedisTemplate {
        return ReactiveStringRedisTemplate(lettuceConnectionFactory)
    }

}