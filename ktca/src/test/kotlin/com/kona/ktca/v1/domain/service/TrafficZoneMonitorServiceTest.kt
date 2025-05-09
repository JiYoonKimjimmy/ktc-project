package com.kona.ktca.v1.domain.service

import io.kotest.core.spec.style.BehaviorSpec

class TrafficZoneMonitorServiceTest : BehaviorSpec({

    given("전체 트래픽 제어 Zone 모니터링 요청되어") {

        `when`("현재 트래픽 제어 활성화된 Zone 없는 경우") {

            then("Zone 현황 조회 0건 정상 확인한다") {

            }
        }

        `when`("현재 트래픽 제어 활성화된 Zone 2건인 경우") {

            then("Zone 현황 조회 결과 정상 확인한다") {

            }

            then("'test-zone-1' 트래픽 현황 결과 정상 확인한다") {

            }

            then("'test-zone-2' 트래픽 현황 결과 정상 확인한다") {

            }
        }

        `when`("특정 zoneId 기준 트래픽 제어 Zone 모니터링 조회인 경우") {

            then("'test-zone-2' 트래픽 현황 결과 정상 확인한다") {

            }
        }
    }

    given("특정 'zoneId' 기준 트래픽 제어 Zone 모니터링 요청되어") {

        `when`("요청 'zoneId' 일치한 트래픽 제어 Zone 없는 경우") {

            then("0건 반환 처리 정상 확인한다") {

            }
        }

        `when`("요청 'zoneId' 일치한 트래픽 제어 Zone 있는 경우") {

            then("'test-zone-2' 트래픽 현황 결과 정상 확인한다") {

            }
        }
    }

})