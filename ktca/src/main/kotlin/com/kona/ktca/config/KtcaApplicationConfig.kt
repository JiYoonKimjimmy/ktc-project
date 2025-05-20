package com.kona.ktca.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories("com.kona.ktca")
@ComponentScan("com.kona.common")
@Configuration
class KtcaApplicationConfig