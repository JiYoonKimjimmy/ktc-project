package com.kona.ktca.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@EnableAsync
@EnableScheduling
@Configuration
class SchedulerConfig {

    @Bean(name = ["trafficTokenExpireSchedulerTaskExecutor"])
    fun trafficTokenExpireSchedulerTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 30
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("traffic-expiration-")
        executor.initialize()
        return executor
    }

    @Bean(name = ["trafficZoneMonitorCollectSchedulerTaskExecutor"])
    fun trafficZoneMonitorCollectSchedulerTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 30
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("traffic-zone-monitoring-")
        executor.initialize()
        return executor
    }

}