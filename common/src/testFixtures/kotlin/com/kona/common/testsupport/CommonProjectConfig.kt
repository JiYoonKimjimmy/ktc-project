package com.kona.common.testsupport

import com.kona.common.testsupport.redis.EmbeddedRedisTestListener
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode

open class CommonProjectConfig : AbstractProjectConfig() {

    override fun extensions(): List<Extension> {
        return listOf(SpringTestExtension(SpringTestLifecycleMode.Root), EmbeddedRedisTestListener())
    }

}