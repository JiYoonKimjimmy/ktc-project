package com.kona.ktc.testsupport

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.kona.ktc.testsupport.redis.EmbeddedRedis
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class KtcProjectConfig : AbstractProjectConfig() {

    companion object {
        val objectMapper: ObjectMapper by lazy {
            jacksonObjectMapper().registerModule(kotlinModule())
        }

        fun mockMvcBuilder(controller: Any): MockMvc {
            return MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
                .build()
        }
    }

    override fun extensions(): List<Extension> = listOf(SpringTestExtension(SpringTestLifecycleMode.Root))

    override suspend fun beforeProject() {
        EmbeddedRedis.embeddedRedisStart()
    }

    override suspend fun afterProject() {
        EmbeddedRedis.embeddedRedisStop()
    }
}