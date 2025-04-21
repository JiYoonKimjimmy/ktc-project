package com.kona.ktc.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan(basePackages = ["com.kona.ktc", "com.kona.common"])
@Configuration
class KtcApplicationConfig