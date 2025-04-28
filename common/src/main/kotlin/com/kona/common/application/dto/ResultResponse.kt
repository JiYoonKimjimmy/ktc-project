package com.kona.common.application.dto

import com.kona.common.enumerate.ResultStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.FeatureCode
import com.kona.common.infrastructure.util.COMPONENT_CODE

data class ResultResponse(
    val status: ResultStatus = ResultStatus.SUCCESS,
    val code: String? = null,
    val message: String? = null,
    val detailMessage: String? = null
) {

    constructor(featureCode: FeatureCode, errorCode: ErrorCode, detailMessage: String? = null): this(
        status = ResultStatus.FAILED,
        code = "${COMPONENT_CODE}_${featureCode.code}_${errorCode.code}",
        message = "${featureCode.message} failed. ${errorCode.message}.",
        detailMessage = detailMessage
    )

}