package com.kona.common.infrastructure.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import java.time.Duration

class RedissonConfig(
    private val activeProfile: String,
    redisProperties: RedisProperties
) {

    private var host: String = redisProperties.host
    private var port: String = redisProperties.port.toString()
    private var password: String = redisProperties.password
    private var timeout: Duration = redisProperties.timeout
    private var connectTimeout: Duration = redisProperties.connectTimeout
    private var nodes: List<String> = when (this.activeProfile) {
        "test" -> emptyList()
        else -> redisProperties.cluster.nodes
    }
    private var minIdle: Int = redisProperties.lettuce.pool.minIdle
    private var maxActive: Int = redisProperties.lettuce.pool.maxActive

    fun redissonClient(): RedissonClient {
        return Redisson.create(redissonClientConfig())
    }

    private fun redissonClientConfig(): Config {
        val config = when (this.activeProfile) {
            "test" -> this::redissonSingleServerConfig
            "prod" -> this::redissonElastiCacheClusterServersConfig
            else -> this::redissonClusterServersConfig
        }
        return Config().apply(config)
    }

    private fun redissonSingleServerConfig(config: Config) {
        config
            .useSingleServer()
            .setAddress("redis://$host:$port")
            .setTimeout(timeout.toMillis().toInt())
            .setConnectTimeout(connectTimeout.toMillis().toInt())
            .setConnectionMinimumIdleSize(minIdle)
            .setConnectionPoolSize(maxActive)
    }

    private fun redissonClusterServersConfig(config: Config) {
        config
            .useClusterServers()
            .addNodeAddress(*nodes.toRedissonAddresses())
            .setPassword(password)
            .setTimeout(timeout.toMillis().toInt())
            .setConnectTimeout(connectTimeout.toMillis().toInt())
            .setMasterConnectionMinimumIdleSize(minIdle)
            .setMasterConnectionPoolSize(maxActive)
            .setSlaveConnectionMinimumIdleSize(minIdle)
            .setSlaveConnectionPoolSize(maxActive)
    }

    /**
     * AWS ElastiCache Serverless - cluster, no pw, apply ssl/tls
     */
    private fun redissonElastiCacheClusterServersConfig(config: Config) {
        config
            .useClusterServers()
            .addNodeAddress(*nodes.toRedissonTlsAddresses())
            .setTimeout(timeout.toMillis().toInt())
            .setConnectTimeout(connectTimeout.toMillis().toInt())
            .setMasterConnectionMinimumIdleSize(minIdle)
            .setMasterConnectionPoolSize(maxActive)
            .setSlaveConnectionMinimumIdleSize(minIdle)
            .setSlaveConnectionPoolSize(maxActive)
    }

    private fun List<String>.toRedissonAddresses(): Array<String> {
        return this.map { "redis://$it" }.toTypedArray()
    }

    private fun List<String>.toRedissonTlsAddresses(): Array<String> {
        return this.map { "rediss://$it" }.toTypedArray()
    }

}