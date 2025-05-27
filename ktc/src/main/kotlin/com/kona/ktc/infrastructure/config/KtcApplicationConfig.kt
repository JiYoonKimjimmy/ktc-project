package com.kona.ktc.infrastructure.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@ComponentScan(basePackages = ["com.kona.ktc", "com.kona.common"])
@Configuration
class KtcApplicationConfig