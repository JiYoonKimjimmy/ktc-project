package com.kona.ktca.config

import com.kona.common.infrastructure.error.FeatureCode
import com.kona.common.infrastructure.error.handler.BaseExceptionHandler
import com.kona.ktca.api.V1ZoneManagementApiController
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.RestControllerAdvice

@Order(value = Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [V1ZoneManagementApiController::class])
class V1ZoneManagementApiControllerAdvice : BaseExceptionHandler(FeatureCode.V1_TRAFFIC_ZONE_MANAGEMENT_SERVICE)