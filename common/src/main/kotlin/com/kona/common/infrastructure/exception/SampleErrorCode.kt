package com.kona.common.infrastructure.exception

enum class SampleErrorCode(
    val statusCode: Int,
    val errorCode: String,
    val errorMessage: String
) {

    /**
     * 에러코드 정의 형식 : 컴포넌트ID_카테고리_에러코드
     */
    PHONE_NUMBER_DUPLICATE(400, "789_0000_001", "Phone Number Duplicate"),
    MEMBER_NOT_FOUND(400, "789_0000_002", "Member not found");
}
