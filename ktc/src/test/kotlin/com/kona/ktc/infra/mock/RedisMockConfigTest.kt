package com.kona.ktc.infra.mock

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate

@SpringBootTest
class RedisMockConfigTest @Autowired constructor(
    private val stringRedisTemplate: StringRedisTemplate,
) : DescribeSpec({

    beforeEach {
        // 각 테스트 전에 Redis 데이터 초기화
        stringRedisTemplate.execute { connection -> connection.serverCommands().flushAll() }
    }

    afterSpec {
        // 리소스 정리
        stringRedisTemplate.execute { connection -> connection.close() }
    }

    describe("Redis 서비스 테스트") {
        it("RedisTemplate 사용한 기본 작업 테스트") {
            // 값 저장
            stringRedisTemplate.opsForValue().set("testKey", "testValue")

            // 값 조회
            val value = stringRedisTemplate.opsForValue().get("testKey")
            value shouldBe "testValue"

            // 값 삭제
            stringRedisTemplate.delete("testKey")
            stringRedisTemplate.hasKey("testKey") shouldBe false
        }

        it("List 작업 테스트") {
            // List 데이터 추가
            stringRedisTemplate.opsForList().rightPush("testList", "value1")
            stringRedisTemplate.opsForList().rightPush("testList", "value2")

            // List 조회
            val list = stringRedisTemplate.opsForList().range("testList", 0, -1)
            list shouldBe listOf("value1", "value2")
        }

        it("Hash 작업 테스트") {
            // Hash 데이터 추가
            stringRedisTemplate.opsForHash<String, String>().put("testHash", "field1", "value1")
            stringRedisTemplate.opsForHash<String, String>().put("testHash", "field2", "value2")

            // Hash 조회
            val value1 = stringRedisTemplate.opsForHash<String, String>().get("testHash", "field1")
            val value2 = stringRedisTemplate.opsForHash<String, String>().get("testHash", "field2")
            value1 shouldBe "value1"
            value2 shouldBe "value2"
        }
    }

})