package com.kona.ktca.infrastructure.config

import com.kona.common.infrastructure.error.FeatureCode
import com.kona.common.infrastructure.error.handler.BaseExceptionHandler
import com.kona.ktca.api.V1MemberManagementApiController
import com.kona.ktca.api.V1ZoneManagementApiController
import com.kona.ktca.api.V1ZoneMonitoringApiController
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.RestControllerAdvice

@Order(value = Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [V1ZoneManagementApiController::class])
class V1ZoneManagementApiControllerAdvice : BaseExceptionHandler(FeatureCode.V1_TRAFFIC_ZONE_MANAGEMENT_SERVICE)

@Order(value = Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [V1ZoneMonitoringApiController::class])
class V1ZoneMonitoringApiControllerAdvice : BaseExceptionHandler(FeatureCode.V1_TRAFFIC_ZONE_MONITORING_SERVICE)

@Order(value = Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [V1MemberManagementApiController::class])
class V1MemberManagementApiControllerAdvice : BaseExceptionHandler(FeatureCode.V1_MEMBER_MANAGEMENT_SERVICE)