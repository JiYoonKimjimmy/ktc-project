package com.kona.common.infrastructure.error.handler

import com.kona.common.infrastructure.error.FeatureCode

class TrafficControllerAdvice : BaseExceptionHandler(FeatureCode.V1_TRAFFIC_CONTROL_SERVICE)