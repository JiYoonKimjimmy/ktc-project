package com.kona.common.infrastructure.config

import io.lettuce.core.SocketOptions
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisNode
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import java.time.Duration

class LettuceConfig(
    private val activeProfile: String,
    private val redisProperties: RedisProperties
) {

    fun lettuceConnectionFactory(): LettuceConnectionFactory {
        return when (activeProfile) {
            "test" -> lettuceStandaloneConnectionFactory()
            "prod" -> lettuceElastiCacheClusterConnectionFactory()
            else -> lettuceClusterConnectionFactory()
        }
    }

    private fun lettuceStandaloneConnectionFactory(): LettuceConnectionFactory {
        val host = redisProperties.host
        val port = redisProperties.port
        val password = redisProperties.password

        val lettuceClusterConfiguration = RedisStandaloneConfiguration(host, port)
        lettuceClusterConfiguration.setPassword(password)
        return LettuceConnectionFactory(lettuceClusterConfiguration)
    }

    private fun lettuceClusterConnectionFactory(): LettuceConnectionFactory {
        val nodes = redisProperties.cluster.nodes.clusterNodes()
        val password = redisProperties.password
        val maxRedirects = redisProperties.cluster.maxRedirects

        val lettuceClusterConfiguration = RedisClusterConfiguration().apply {
            this.setClusterNodes(nodes)
            this.setPassword(password)
            this.maxRedirects = maxRedirects
        }

        val socketOptions = SocketOptions.builder()
            .connectTimeout(redisProperties.connectTimeout)
            .keepAlive(true)
            .build()

        val clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .dynamicRefreshSources(true)
            .enableAllAdaptiveRefreshTriggers()
            .enablePeriodicRefresh(Duration.ofMinutes(30))
            .build()

        val clusterClientOptions = ClusterClientOptions.builder()
            .topologyRefreshOptions(clusterTopologyRefreshOptions)
            .socketOptions(socketOptions)
            .build()

        val lettuceClientConfiguration = LettuceClientConfiguration.builder()
            .clientOptions(clusterClientOptions)
            .commandTimeout(redisProperties.timeout)
            .build()

        return LettuceConnectionFactory(lettuceClusterConfiguration, lettuceClientConfiguration)
    }

    /**
     * AWS ElastiCache Serverless - cluster, no pw, apply ssl/tls
     */
    private fun lettuceElastiCacheClusterConnectionFactory(): LettuceConnectionFactory {
        val nodes = redisProperties.cluster.nodes.clusterNodes()
        val maxRedirects = redisProperties.cluster.maxRedirects

        val lettuceClusterConfiguration = RedisClusterConfiguration().apply {
            this.setClusterNodes(nodes)
            this.maxRedirects = maxRedirects
        }

        val socketOptions = SocketOptions.builder()
            .connectTimeout(redisProperties.connectTimeout)
            .keepAlive(true)
            .build()

        val clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .dynamicRefreshSources(true)
            .enableAllAdaptiveRefreshTriggers()
            .enablePeriodicRefresh(Duration.ofMinutes(30))
            .build()

        val clusterClientOptions = ClusterClientOptions.builder()
            .topologyRefreshOptions(clusterTopologyRefreshOptions)
            .socketOptions(socketOptions)
            .build()

        val lettuceClientConfiguration = LettuceClientConfiguration.builder()
            .clientOptions(clusterClientOptions)
            .commandTimeout(redisProperties.timeout)
            .useSsl()
            .build()

        return LettuceConnectionFactory(lettuceClusterConfiguration, lettuceClientConfiguration)
    }

    private fun List<String>.clusterNodes(): List<RedisNode> {
        return this.map {
            val host = it.split(":")[0]
            val port = it.split(":")[1].toInt()
            RedisNode(host, port)
        }
    }

}