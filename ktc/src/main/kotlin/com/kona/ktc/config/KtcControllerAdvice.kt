package com.kona.ktc.config

import com.kona.common.infrastructure.error.FeatureCode
import com.kona.common.infrastructure.error.handler.BaseExceptionHandler
import com.kona.ktc.v1.application.adapter.inbound.TrafficControlAdapter
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.RestControllerAdvice

@Order(value = Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [TrafficControlAdapter::class])
class TrafficControlAdapterAdvice : BaseExceptionHandler(FeatureCode.V1_TRAFFIC_CONTROL_SERVICE)