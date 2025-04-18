package com.kona.ktc.v0.domain.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class TrafficWaitingTest : BehaviorSpec({
    given("TrafficWaiting 생성 시") {
        val number = 1L
        val estimatedTime = 300L
        val totalCount = 10L

        `when`("기본 폴링 주기를 사용하면") {
            val waiting = TrafficWaiting(
                number = number,
                estimatedTime = estimatedTime,
                totalCount = totalCount
            )

            then("모든 필드가 정상적으로 설정되고, 폴링 주기는 5초여야 한다") {
                waiting.number shouldBe number
                waiting.estimatedTime shouldBe estimatedTime
                waiting.totalCount shouldBe totalCount
                waiting.poolingPeriod shouldBe 5L
            }
        }

        `when`("커스텀 폴링 주기를 지정하면") {
            val customPoolingPeriod = 10L

            val waiting = TrafficWaiting(
                number = number,
                estimatedTime = estimatedTime,
                totalCount = totalCount,
                poolingPeriod = customPoolingPeriod
            )

            then("모든 필드가 정상적으로 설정되고, 폴링 주기는 지정한 값이어야 한다") {
                waiting.number shouldBe number
                waiting.estimatedTime shouldBe estimatedTime
                waiting.totalCount shouldBe totalCount
                waiting.poolingPeriod shouldBe customPoolingPeriod
            }
        }
    }
}) 