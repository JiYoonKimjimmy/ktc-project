package com.kona.ktca.v1.application.controller

import com.kona.ktca.api.ZoneApiDelegate
import com.kona.ktca.dto.GetAllZoneInfoResDto
import com.kona.ktca.dto.GetZoneInfoResDto
import com.kona.ktca.dto.PostZoneInfoReqDto
import com.kona.ktca.dto.PostZoneInfoResDto
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class ZoneApiController: ZoneApiDelegate {

    override fun deleteZoneInfo(zoneId: String): ResponseEntity<Unit> {
        return ResponseEntity.ok().build()
    }

    override fun getAllZoneInfo(): ResponseEntity<GetAllZoneInfoResDto> {
        return super.getAllZoneInfo()
    }

    override fun getZoneInfo(zoneId: String): ResponseEntity<GetZoneInfoResDto> {
        return super.getZoneInfo(zoneId)
    }

    override fun postZoneInfo(postZoneInfoReqDto: PostZoneInfoReqDto): ResponseEntity<PostZoneInfoResDto> {
        return super.postZoneInfo(postZoneInfoReqDto)
    }

}