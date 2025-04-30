package com.kona.common.infrastructure.redis

import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.common.testsupport.redis.EmbeddedRedisTestListener
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.redis.core.script.DefaultRedisScript

class RedisExecuteAdapterImplTest : StringSpec({

    listeners(EmbeddedRedisTestListener())

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate
    val redisScriptExecuteAdapter = RedisExecuteAdapterImpl(reactiveStringRedisTemplate)

    "Redis script 실행 결과 정상 동작 확인한다" {
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

    "pattern 일치하는 cache key 목록 조회 결과 정상 확인한다" {
        // given
        val pattern = "test:*"

        listOf("test:1", "test:2", "test:3", "other:1").forEach { reactiveStringRedisTemplate.opsForValue().set(it, "value").awaitSingle() }

        // when
        val result = redisScriptExecuteAdapter.keys(pattern)

        // then
        result.shouldNotBeEmpty()
        result shouldHaveSize 3
        result shouldContainAll listOf("test:1", "test:2", "test:3")
    }

    "pattern 일치하는 cache key 없는 경우 emptyList 정상 확인한다" {
        // given
        val pattern = "nonexistent:*"

        listOf("test:1", "test:2", "test:3").forEach { reactiveStringRedisTemplate.opsForValue().set(it, "value").awaitSingle() }

        // when
        val result = redisScriptExecuteAdapter.keys(pattern)

        // then
        result.shouldBeEmpty()
    }
})