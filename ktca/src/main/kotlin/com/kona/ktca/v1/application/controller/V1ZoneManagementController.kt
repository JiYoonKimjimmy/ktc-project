package com.kona.ktca.v1.application.controller

import com.kona.ktca.api.V1ZoneManagementApi
import com.kona.ktca.api.V1ZoneManagementApiDelegate
import com.kona.ktca.dto.V1FindAllZoneResponse
import com.kona.ktca.dto.V1FindZoneResponse
import com.kona.ktca.dto.V1SaveZoneRequest
import com.kona.ktca.dto.V1SaveZoneResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class V1ZoneManagementController: V1ZoneManagementApiDelegate {

    override fun deleteZone(zoneId: String): ResponseEntity<Unit> {
        return super.deleteZone(zoneId)
    }

    override fun findZone(zoneId: String): ResponseEntity<V1FindZoneResponse> {
        return super.findZone(zoneId)
    }

    override fun findZoneList(page: Int?, size: Int?, zoneId: String?): ResponseEntity<V1FindAllZoneResponse> {
        return super.findZoneList(page, size, zoneId)
    }

    override fun saveZone(v1SaveZoneRequest: V1SaveZoneRequest): ResponseEntity<V1SaveZoneResponse> {
        return super.saveZone(v1SaveZoneRequest)
    }

}