package com.kona.ktc.infrastructure.adapter.redis

import com.kona.ktc.domain.model.Traffic
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.system.measureTimeMillis
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@ActiveProfiles("test")
@SpringBootTest
class TrafficControlScriptExecuteAdapterPerformanceTest(
    private val trafficControlScriptExecuteAdapter: TrafficControlScriptExecuteAdapter
) : DescribeSpec({

    describe("'traffic-control.lua' 스크립트 성능 테스트") {

        context("동시 요청 건수별 성능 테스트") {
            forAll(
                row("1K", 1_000),
                row("10K", 10_000),
//                row("50K", 50_000),
//                row("100K", 100_000),
            ) { description, count ->
                it("$description 동시 요청 성능 측정") {
                    val zoneId = "test-zone"
                    val requestIndex = AtomicInteger(0)
                    val maxResponseIndex = AtomicInteger(0)
                    val maxResponseTime = AtomicLong(0L)
                    val totalResponseTime = AtomicLong(0L)
                    val overMinCount = AtomicInteger(0)

                    val executionTime = measureTimeMillis {
                        runBlocking {
                            coroutineScope {
                                val jobs = List(count) { index ->
                                    async {
                                        val traffic = Traffic(
                                            zoneId = zoneId,
                                            token = "test-token-$index"
                                        )
                                        val requestTime = measureTimeMillis {
                                            trafficControlScriptExecuteAdapter.controlTraffic(traffic)
                                        }
                                        
                                        if (requestTime > maxResponseTime.get()) {
                                            maxResponseIndex.set(requestIndex.get())
                                            maxResponseTime.set(requestTime)
                                        }

                                        if (requestTime > 1000) {
                                            overMinCount.incrementAndGet()
                                        }

                                        totalResponseTime.addAndGet(requestTime)
                                        requestIndex.incrementAndGet()
                                    }
                                }
                                jobs.awaitAll()
                            }
                        }
                    }

                    val averageTimePerRequest = totalResponseTime.get().toDouble() / count

                    println("""
                        | $description 건 동시 요청 성능:
                        | - 총 처리 시간 : ${executionTime}ms
                        | - 평균 처리 시간 : ${averageTimePerRequest}ms
                        | - 최대 처리 시간 : ${maxResponseTime.get()}ms
                        | - 최대 처리 Index : ${maxResponseIndex.get()}
                        | - 완료 처리 Index : ${requestIndex.get()}
                        | - 1000ms 초과 처리 건수 : ${overMinCount.get()}
                    """.trimMargin())
                }
            }
        }

        context("순차 요청 건수별 성능 테스트") {
            forAll(
                row("1K", 1_000),
                row("10K", 10_000),
//                row("50K", 50_000),
//                row("100K", 100_000),
            ) { description, count ->
                it("$description 동시 요청 성능 측정") {
                    val zoneId = "test-zone"
                    var maxResponseTime = 0L

                    val executionTime = measureTimeMillis {
                        runBlocking {
                            repeat(count) { index ->
                                val traffic = Traffic(
                                    zoneId = zoneId,
                                    token = "test-token-$index"
                                )
                                val requestTime = measureTimeMillis {
                                    trafficControlScriptExecuteAdapter.controlTraffic(traffic)
                                }
                                maxResponseTime = maxOf(maxResponseTime, requestTime)
                            }
                        }
                    }

                    val averageTimePerRequest = executionTime.toDouble() / count

                    println("""
                        | $description 건 순차 요청 성능:
                        | - 총 처리 시간 : ${executionTime}ms
                        | - 평균 처리 시간 : ${averageTimePerRequest}ms
                        | - 최대 처리 시간 : ${maxResponseTime}ms
                    """.trimMargin())
                }
            }
        }

    }

})