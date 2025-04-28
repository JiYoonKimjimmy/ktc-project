package com.kona.common.infrastructure.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import java.net.InetAddress
import java.text.DecimalFormat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

class SnowflakeIdGeneratorTest : DescribeSpec({

    describe("SnowflakeIdGenerator") {
        it("생성된 ID는 숫자로만 구성되어야 한다") {
            val id = SnowflakeIdGenerator.generate()
            id shouldMatch Regex("^\\d+$")
        }

        it("생성된 ID의 길이는 19자리 이하여야 한다") {
            val id = SnowflakeIdGenerator.generate()
            println("생성된 ID: $id (길이: ${id.length})")
            id.length shouldBeLessThanOrEqual 19
        }

        it("연속적으로 생성된 ID는 서로 달라야 한다") {
            val id1 = SnowflakeIdGenerator.generate()
            val id2 = SnowflakeIdGenerator.generate()
            id1 shouldNotBe id2
        }

        it("동시에 여러 ID를 생성해도 중복이 없어야 한다") {
            val threadCount = 10
            val idCount = 1000
            val executor = Executors.newFixedThreadPool(threadCount)
            val latch = CountDownLatch(threadCount)
            val generatedIds = mutableSetOf<String>()
            val lock = Any()

            repeat(threadCount) {
                executor.submit {
                    repeat(idCount) {
                        val id = SnowflakeIdGenerator.generate()
                        synchronized(lock) {
                            generatedIds.add(id)
                        }
                    }
                    latch.countDown()
                }
            }

            latch.await(5, TimeUnit.SECONDS)
            generatedIds.size shouldBe (threadCount * idCount)
        }

        it("워커 ID는 0에서 MAX_WORKER_ID 사이의 값이어야 한다") {
            val workerId = SnowflakeIdGenerator::class.java.getDeclaredField("workerId").apply { isAccessible = true }
                .get(SnowflakeIdGenerator) as Long
            workerId shouldBe (InetAddress.getLocalHost().hostAddress.split(".")[3].toLong() % (SnowflakeIdGenerator.MAX_WORKER_ID + 1))
        }

        it("시계가 역행하면 예외가 발생해야 한다") {
            val timestampField = SnowflakeIdGenerator::class.java.getDeclaredField("lastTimestamp").apply { isAccessible = true }
            val originalTimestamp = timestampField.get(SnowflakeIdGenerator) as Long
            
            try {
                // 시계를 역행시킴
                timestampField.set(SnowflakeIdGenerator, System.currentTimeMillis() + 1000)
                
                // ID 생성 시도
                SnowflakeIdGenerator.generate()
            } catch (e: RuntimeException) {
                e.message shouldBe "Clock moved backwards"
            } finally {
                // 원래 상태로 복구
                timestampField.set(SnowflakeIdGenerator, originalTimestamp)
            }
        }

        it("같은 밀리초에 여러 ID를 생성할 수 있어야 한다") {
            val ids = mutableSetOf<String>()
            val startTime = System.currentTimeMillis()
            
            // 같은 밀리초 내에 여러 ID 생성 시도
            while (System.currentTimeMillis() - startTime < 1) {
                ids.add(SnowflakeIdGenerator.generate())
            }
            
            // 같은 밀리초 내에 여러 ID가 생성되어야 함
            ids.size shouldBeGreaterThanOrEqual 1
        }

        it("MAX_WORKER_ID는 올바른 값이어야 한다") {
            SnowflakeIdGenerator.MAX_WORKER_ID shouldBe 31L  // 2^5 - 1
        }

        it("MAX_SEQUENCE는 올바른 값이어야 한다") {
            SnowflakeIdGenerator.MAX_SEQUENCE shouldBe 4095L  // 2^12 - 1
        }

        context("성능 테스트") {
            val decimalFormat = DecimalFormat("#,##0.00")
            
            forAll(
                row("1K IDs", 1_000),
                row("10K IDs", 10_000),
                row("100K IDs", 100_000)
            ) { description, count ->
                it("$description 생성 성능 측정") {
                    val executionTime = measureNanoTime {
                        repeat(count) {
                            SnowflakeIdGenerator.generate()
                        }
                    }

                    val idsPerSecond = (count.toDouble() / executionTime.toDouble()) * 1_000_000_000
                    val totalTimeMs = executionTime / 1_000_000.0
                    val avgTimeNs = executionTime.toDouble() / count
                    val avgTimeMs = avgTimeNs / 1_000_000.0
                    
                    println("""
                        |$description 생성 성능:
                        |  - 처리량: ${decimalFormat.format(idsPerSecond)} IDs/sec
                        |  - 총 소요시간: ${decimalFormat.format(totalTimeMs)} ms
                        |  - 평균 처리시간: ${decimalFormat.format(avgTimeMs)} ms/ID (${decimalFormat.format(avgTimeNs)} ns/ID)
                    """.trimMargin())
                    
                    // 최소 성능 기준 설정 (예: 초당 100,000 IDs)
                    idsPerSecond shouldBeGreaterThan 100_000.0
                }
            }

            it("멀티스레드 환경에서의 성능 측정") {
                val threadCount = 4
                val idsPerThread = 25_000 // 총 100K IDs
                val executor = Executors.newFixedThreadPool(threadCount)
                val latch = CountDownLatch(threadCount)

                val executionTime = measureNanoTime {
                    repeat(threadCount) {
                        executor.submit {
                            repeat(idsPerThread) {
                                SnowflakeIdGenerator.generate()
                            }
                            latch.countDown()
                        }
                    }
                    latch.await(5, TimeUnit.SECONDS)
                }

                val totalIds = threadCount * idsPerThread
                val idsPerSecond = (totalIds.toDouble() / executionTime.toDouble()) * 1_000_000_000
                val totalTimeMs = executionTime / 1_000_000.0
                val avgTimeNs = executionTime.toDouble() / totalIds
                val avgTimeMs = avgTimeNs / 1_000_000.0
                
                println("""
                    |멀티스레드(4) 100K IDs 생성 성능:
                    |  - 처리량: ${decimalFormat.format(idsPerSecond)} IDs/sec
                    |  - 총 소요시간: ${decimalFormat.format(totalTimeMs)} ms
                    |  - 평균 처리시간: ${decimalFormat.format(avgTimeMs)} ms/ID (${decimalFormat.format(avgTimeNs)} ns/ID)
                    |  - 스레드당 처리량: ${decimalFormat.format(idsPerSecond / threadCount)} IDs/sec/thread
                """.trimMargin())
                
                // 멀티스레드 환경에서의 최소 성능 기준
                idsPerSecond shouldBeGreaterThan 200_000.0
            }
        }
    }

}) 