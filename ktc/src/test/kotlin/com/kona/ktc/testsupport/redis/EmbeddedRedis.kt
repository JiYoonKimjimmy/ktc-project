package com.kona.ktc.testsupport.redis

import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
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

    fun embeddedRedisStart() {
        if (!redisServer.isActive) {
            redisServer.start()
            logger.info("Embedded Redis Server Started!!")
        }
    }

    fun embeddedRedisStop() {
        redisServer.stop()
        logger.info("Embedded Redis Server Stopped!!")
    }

    private fun redisConnectionFactory(): LettuceConnectionFactory {
        val redisSentinelConfiguration = RedisStandaloneConfiguration(REDIS_HOST, REDIS_PORT)
        return LettuceConnectionFactory(redisSentinelConfiguration).also { it.afterPropertiesSet() }
    }

    private fun stringRedisTemplate(): StringRedisTemplate {
        return StringRedisTemplate(lettuceConnectionFactory).also { it.afterPropertiesSet() }
    }

//    val redissonClient: RedissonClient by lazy { redissonClient() }
//    private fun redissonClient(): RedissonClient {
//        return Redisson.create(Config().apply(this::useSingleServerTestConfig))
//    }
//    private fun useSingleServerTestConfig(config: Config) {
//        config
//            .useSingleServer()
//            .setAddress("redis://${REDIS_HOST}:${REDIS_PORT}")
//    }

}