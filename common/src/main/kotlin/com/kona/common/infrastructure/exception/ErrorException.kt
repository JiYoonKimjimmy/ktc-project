package com.kona.common.infrastructure.exception

class ErrorException : RuntimeException {
    val sampleErrorCode: SampleErrorCode
    val additionalMessage: String


    constructor(sampleErrorCode: SampleErrorCode) : super(sampleErrorCode.errorMessage) {
        this.sampleErrorCode = sampleErrorCode
        this.additionalMessage = ""
    }

    constructor(
        sampleErrorCode: SampleErrorCode,
        additionalMessage: String
    ) : super(sampleErrorCode.errorMessage + " - " + additionalMessage) {
        this.sampleErrorCode = sampleErrorCode
        this.additionalMessage = " - $additionalMessage"
    }
}
