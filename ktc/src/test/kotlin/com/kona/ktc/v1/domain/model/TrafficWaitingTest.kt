package com.kona.ktc.v1.domain.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TrafficWaitingTest : BehaviorSpec({
    given("TrafficWaiting 생성 시") {
        val number = 10L
        val estimatedTime = 1000L
        val totalCount = 100L

        `when`("기본 poolingPeriod로 생성하면") {
            val trafficWaiting = TrafficWaiting(
                number = number,
                estimatedTime = estimatedTime,
                totalCount = totalCount
            )

            then("기본 poolingPeriod가 설정된다") {
                trafficWaiting.poolingPeriod shouldBe 5000L
            }

            then("다른 필드들이 정상적으로 설정된다") {
                trafficWaiting.number shouldBe number
                trafficWaiting.estimatedTime shouldBe estimatedTime
                trafficWaiting.totalCount shouldBe totalCount
            }
        }

        `when`("custom poolingPeriod로 생성하면") {
            val customPoolingPeriod = 10L
            val trafficWaiting = TrafficWaiting(
                number = number,
                estimatedTime = estimatedTime,
                totalCount = totalCount,
                poolingPeriod = customPoolingPeriod
            )

            then("custom poolingPeriod가 설정된다") {
                trafficWaiting.poolingPeriod shouldBe customPoolingPeriod
            }
        }

        `when`("동일한 필드값으로 생성된 두 객체를 비교하면") {
            val trafficWaiting1 = TrafficWaiting(
                number = number,
                estimatedTime = estimatedTime,
                totalCount = totalCount
            )
            val trafficWaiting2 = TrafficWaiting(
                number = number,
                estimatedTime = estimatedTime,
                totalCount = totalCount
            )

            then("동등성 비교가 성공한다") {
                trafficWaiting1 shouldBe trafficWaiting2
            }

            then("해시코드가 동일하다") {
                trafficWaiting1.hashCode() shouldBe trafficWaiting2.hashCode()
            }
        }

        `when`("다른 필드값으로 생성된 두 객체를 비교하면") {
            val trafficWaiting1 = TrafficWaiting(
                number = number,
                estimatedTime = estimatedTime,
                totalCount = totalCount
            )
            val trafficWaiting2 = TrafficWaiting(
                number = 20L,
                estimatedTime = 2000L,
                totalCount = 200L
            )

            then("동등성 비교가 실패한다") {
                trafficWaiting1 shouldNotBe trafficWaiting2
            }

            then("해시코드가 다르다") {
                trafficWaiting1.hashCode() shouldNotBe trafficWaiting2.hashCode()
            }
        }
    }
}) 