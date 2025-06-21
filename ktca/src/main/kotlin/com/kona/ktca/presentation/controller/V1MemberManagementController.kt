package com.kona.ktca.presentation.controller

import com.kona.common.infrastructure.enumerate.MemberRole
import com.kona.common.infrastructure.enumerate.MemberStatus
import com.kona.ktca.api.V1MemberManagementApiDelegate
import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.port.inbound.MemberFindPort
import com.kona.ktca.domain.port.inbound.MemberSavePort
import com.kona.ktca.dto.Pageable
import com.kona.ktca.dto.V1CreateMemberRequest
import com.kona.ktca.dto.V1CreateMemberResponse
import com.kona.ktca.dto.V1FindAllMemberResponse
import com.kona.ktca.dto.V1FindMemberResponse
import com.kona.ktca.dto.V1UpdateMemberRequest
import com.kona.ktca.dto.V1UpdateMemberResponse
import com.kona.ktca.presentation.model.V1MemberModelMapper
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class V1MemberManagementController(
    private val memberSavePort: MemberSavePort,
    private val memberFindPort: MemberFindPort,
    private val v1MemberModelMapper: V1MemberModelMapper
) : V1MemberManagementApiDelegate {

    override fun createMember(
        v1CreateMemberRequest: V1CreateMemberRequest
    ): ResponseEntity<V1CreateMemberResponse> = runBlocking {
        val dto = MemberDTO(
            loginId = v1CreateMemberRequest.loginId,
            password = v1CreateMemberRequest.password,
            name = v1CreateMemberRequest.name,
            email = v1CreateMemberRequest.email,
            team = v1CreateMemberRequest.team,
            role = MemberRole.valueOf(v1CreateMemberRequest.role.name),
        )
        val result = memberSavePort.create(dto)
        val response = V1CreateMemberResponse(memberId = result.memberId!!.toInt())
        ResponseEntity(response, HttpStatus.CREATED)
    }

    override fun findMember(memberId: Int?, loginId: String?): ResponseEntity<V1FindMemberResponse> = runBlocking {
        val dto = MemberDTO(memberId = memberId?.toLong(), loginId = loginId)
        val result = memberFindPort.findMember(dto)
        val response = V1FindMemberResponse(v1MemberModelMapper.domainToModel(result))
        ResponseEntity(response, HttpStatus.OK)
    }

    override fun findMemberList(
        page: Int?,
        size: Int?,
        memberId: Int?,
        loginId: String?,
        name: String?,
        email: String?,
        team: String?,
        role: String?,
        status: String?,
    ): ResponseEntity<V1FindAllMemberResponse> = runBlocking {
        val dto = MemberDTO(
            memberId = memberId?.toLong(),
            loginId = loginId,
            name = name,
            email = email,
            team = team,
            role = role?.let { MemberRole.valueOf(it) },
            status = status?.let { MemberStatus.valueOf(it) }
        )
        val pageable = PageableDTO(number = page ?: 0, size = size ?: 20)
        val result = memberFindPort.findPageMember(dto, pageable)
        val response = V1FindAllMemberResponse(
            pageable = Pageable(
                first = result.isFirst,
                last = result.isLast,
                number = result.number,
                numberOfElements = result.numberOfElements,
                propertySize = result.size,
                totalPages = result.totalPages,
                totalElements = result.totalElements,
            ),
            content = result.content.map { v1MemberModelMapper.domainToModel(it) }
        )
        ResponseEntity(response, HttpStatus.OK)
    }

    override fun updateMember(
        memberId: String,
        v1UpdateMemberRequest: V1UpdateMemberRequest,
    ): ResponseEntity<V1UpdateMemberResponse> = runBlocking {
        val dto = MemberDTO(
            memberId = memberId.toLong(),
            loginId = v1UpdateMemberRequest.loginId,
            password = v1UpdateMemberRequest.password,
            name = v1UpdateMemberRequest.name,
            email = v1UpdateMemberRequest.email,
            team = v1UpdateMemberRequest.team,
            role = v1UpdateMemberRequest.role?.name?.let { MemberRole.valueOf(it) },
            status = v1UpdateMemberRequest.status?.name?.let { MemberStatus.valueOf(it) }
        )
        val result = memberSavePort.update(dto)
        val response = V1UpdateMemberResponse(memberId = result.memberId!!.toInt())
        ResponseEntity(response, HttpStatus.OK)
    }

}