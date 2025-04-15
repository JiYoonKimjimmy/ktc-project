package com.kona.common.util

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*

@Component
class ApplicationHealthIndicator : HealthIndicator {
    override fun health(): Health {
        val gitProperties = Properties()
        try {
            gitProperties.load(ClassPathResource("git.properties").inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val appVersion = gitProperties.getProperty("git.build.version", "unknown")
        val commitId = gitProperties.getProperty("git.commit.id.abbrev", "unknown")
        val buildDateTime = gitProperties.getProperty("git.build.time", "unknown")

        return Health.up()
            .withDetail("version", appVersion)
            .withDetail("hash", commitId.substring(0, 7))
            .withDetail("build-date", buildDateTime)
            .build()
    }
}
