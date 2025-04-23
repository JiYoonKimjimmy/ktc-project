package com.kona.common.infra.config

import jakarta.annotation.PostConstruct
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class RedissonConfig(
    @Value("\${spring.profiles.active}")
    private val environment: String,
    private val redisProperties: RedisProperties
) {

    companion object {
        const val CONNECTION_MIN_IDLE_SIZE = 0
        const val CONNECTION_MAX_POOL_SIZE = 30
    }

    lateinit var host: String
    lateinit var port: String
    lateinit var password: String
    lateinit var timeout: Duration
    lateinit var connectTimeout: Duration
    lateinit var nodes: List<String>

    lateinit var redissonConfig: Config

    @PostConstruct
    fun initialize() {
        this.host = redisProperties.host
        this.port = redisProperties.port.toString()
        this.password = redisProperties.password
        this.timeout = redisProperties.timeout
        this.connectTimeout = redisProperties.connectTimeout
        this.nodes = when (environment) {
            "test" -> emptyList()
            else -> redisProperties.cluster.nodes
        }
        this.redissonConfig = redissonClientConfig()
    }

    @Bean
    fun redissonClient(): RedissonClient {
        return Redisson.create(redissonConfig)
    }

    @Bean
    fun redisConnectionFactory(): RedissonConnectionFactory {
        return RedissonConnectionFactory(redissonConfig)
    }

    private fun redissonClientConfig(): Config {
        val config = when (environment) {
            "test" -> this::useSingleServerConfig
            else -> this::useClusterServersConfig
        }
        return Config().apply(config)
    }

    private fun useSingleServerConfig(config: Config) {
        config
            .useSingleServer()
            .setAddress("redis://$host:$port")
            .setPassword(password)
            .setTimeout(timeout.toMillis().toInt())
            .setConnectTimeout(connectTimeout.toMillis().toInt())
            .setConnectionMinimumIdleSize(CONNECTION_MIN_IDLE_SIZE)
            .setConnectionPoolSize(CONNECTION_MAX_POOL_SIZE)
    }

    private fun useClusterServersConfig(config: Config) {
        config
            .useClusterServers()
            .addNodeAddress(*nodes.toRedissonAddresses())
            .setPassword(password)
            .setTimeout(timeout.toMillis().toInt())
            .setConnectTimeout(connectTimeout.toMillis().toInt())
            .setMasterConnectionMinimumIdleSize(CONNECTION_MIN_IDLE_SIZE)
            .setMasterConnectionPoolSize(CONNECTION_MAX_POOL_SIZE)
            .setSlaveConnectionMinimumIdleSize(CONNECTION_MIN_IDLE_SIZE)
            .setSlaveConnectionPoolSize(CONNECTION_MAX_POOL_SIZE)
    }

    private fun List<String>.toRedissonAddresses(): Array<String> {
        return this.map { "redis://$it" }.toTypedArray()
    }

}