package com.kona.common.infra.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Status
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component

@PropertySource(value = ["classpath:/git.properties"])
@Component(value = "application")
class HealthIndicatorConfig : HealthIndicator {

    @Value("\${git.commit.id.abbrev}")
    lateinit var hash: String

    @Value("\${git.build.version}")
    lateinit var version: String

    @Value("\${git.build.time}")
    lateinit var buildDate: String

    override fun health(): Health {
        return Health
            .Builder(Status.UP, mapOf("version" to this.version, "hash" to this.hash, "build-date" to this.buildDate))
            .build()
    }

}