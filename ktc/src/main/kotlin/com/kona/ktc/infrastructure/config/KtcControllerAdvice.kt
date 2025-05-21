package com.kona.ktc.infrastructure.config

import com.kona.common.infrastructure.error.FeatureCode
import com.kona.common.infrastructure.error.handler.BaseExceptionHandler
import com.kona.ktc.presentation.adapter.V1TrafficControlAdapter
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.RestControllerAdvice

@Order(value = Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [V1TrafficControlAdapter::class])
class V1TrafficControlAdapterAdvice : BaseExceptionHandler(FeatureCode.V1_TRAFFIC_CONTROL_SERVICE)