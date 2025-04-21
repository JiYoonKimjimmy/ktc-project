package com.kona.common.infra.redis.mock

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import redis.embedded.RedisCluster
import redis.embedded.RedisServer

@Profile("test")
@Configuration
class RedisMockConfig(
    private val redisProperties: RedisProperties
) {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val redisCluster: RedisCluster by lazy {
        val nodes = redisProperties.cluster.nodes.map { it.split(":")[1].toInt() }.partition { it % 2 == 0 }
        val masters = nodes.first.map { RedisServer.newRedisServer().port(it).build() }
        val sentinels = nodes.second.map { RedisServer.newRedisServer().port(it).build() }
        RedisCluster(sentinels, masters)
    }

    @PostConstruct
    fun startRedis() {
        if (!redisCluster.isActive) {
            redisCluster.start()
            logger.info("Embedded Redis Cluster Started!!")
        }
    }

    @PreDestroy
    fun stopRedis() {
        redisCluster.stop()
        logger.info("Embedded Redis Cluster Stopped!!")
    }

}