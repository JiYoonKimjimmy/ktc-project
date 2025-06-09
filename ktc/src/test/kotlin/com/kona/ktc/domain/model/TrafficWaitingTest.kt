package com.kona.ktc.domain.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TrafficWaitingTest : BehaviorSpec({
    given("TrafficWaiting 생성 시") {
        val number = 10L
        val estimatedTime = 1000L
        val totalCount = 100L

        `when`("기본 pollingPeriod로 생성하면") {
            val trafficWaiting = TrafficWaiting(
                result = 0,
                number = number,
                estimatedTime = estimatedTime,
                totalCount = totalCount
            )

            then("기본 pollingPeriod가 설정된다") {
                trafficWaiting.pollingPeriod shouldBe 3000L
            }

            then("다른 필드들이 정상적으로 설정된다") {
                trafficWaiting.number shouldBe number
                trafficWaiting.estimatedTime shouldBe estimatedTime
                trafficWaiting.totalCount shouldBe totalCount
            }
        }

        `when`("number 정보가 20_000 이하인 경우") {
            val trafficWaiting = TrafficWaiting(
                result = 0,
                number = 20_000,
                estimatedTime = estimatedTime,
                totalCount = totalCount
            )

            then("pollingPeriod: 3_000 가 설정된다") {
                trafficWaiting.pollingPeriod shouldBe 3_000
            }
        }

        `when`("number 정보가 20_001 ~ 150_000 인 경우") {
            val trafficWaiting = TrafficWaiting(
                result = 0,
                number = 20_001,
                estimatedTime = estimatedTime,
                totalCount = totalCount
            )

            then("pollingPeriod: 6_000 가 설정된다") {
                trafficWaiting.pollingPeriod shouldBe 6_000
            }
        }

        `when`("number 정보가 150_001 이상인 경우") {
            val trafficWaiting = TrafficWaiting(
                result = 0,
                number = 150_001,
                estimatedTime = estimatedTime,
                totalCount = totalCount
            )

            then("pollingPeriod: 6_000 가 설정된다") {
                trafficWaiting.pollingPeriod shouldBe 9_000
            }
        }

        `when`("동일한 필드값으로 생성된 두 객체를 비교하면") {
            val trafficWaiting1 = TrafficWaiting(
                result = 0,
                number = number,
                estimatedTime = estimatedTime,
                totalCount = totalCount
            )
            val trafficWaiting2 = TrafficWaiting(
                result = 0,
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
                result = 0,
                number = number,
                estimatedTime = estimatedTime,
                totalCount = totalCount
            )
            val trafficWaiting2 = TrafficWaiting(
                result = 0,
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