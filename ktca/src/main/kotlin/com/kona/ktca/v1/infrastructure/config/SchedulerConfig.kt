package com.kona.ktca.v1.infrastructure.config

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

    @Bean(name = ["trafficExpirationTaskExecutor"])
    fun trafficExpirationTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2
        executor.maxPoolSize = 4
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("traffic-expiration-")
        executor.initialize()
        return executor
    }

}