package com.kona.common.infrastructure.redis

import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.common.testsupport.redis.EmbeddedRedisTestListener
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.redis.core.script.DefaultRedisScript

class RedisScriptExecuteAdapterImplTest : StringSpec({

    listeners(EmbeddedRedisTestListener())

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate
    val redisScriptExecuteAdapter = RedisScriptExecuteAdapterImpl(reactiveStringRedisTemplate)

    "lua script 실행하여 정상 동작 확인한다" {
        // given
        val scriptString = """
        -- KEYS[1] = key
        local key = KEYS[1]
        
        local value = redis.call("GET", key)
        
        return value
        """.trimIndent()
        val script = DefaultRedisScript(scriptString, List::class.java)
        val key = "test-key"
        val keys = listOf(key)

        reactiveStringRedisTemplate.opsForValue().set(key, "Hello World!").awaitSingle()

        // when
        val result = redisScriptExecuteAdapter.execute(script, keys, emptyList())

        // then
        result.shouldNotBeEmpty()
        result shouldHaveSize 1
        result[0] shouldBe "Hello World!"
    }

})