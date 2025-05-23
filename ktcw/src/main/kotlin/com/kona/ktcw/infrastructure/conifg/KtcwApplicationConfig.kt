package com.kona.ktcw.infrastructure.conifg

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.reactive.config.EnableWebFlux

@EnableScheduling
@EnableWebFlux
@Configuration
class KtcwApplicationConfig