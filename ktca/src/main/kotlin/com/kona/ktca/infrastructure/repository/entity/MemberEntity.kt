package com.kona.ktca.infrastructure.repository.entity

import com.kona.common.infrastructure.enumerate.MemberRole
import com.kona.common.infrastructure.enumerate.MemberStatus
import com.kona.ktca.domain.model.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "MEMBER")
class MemberEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    val loginId: String,
    @Column(nullable = false)
    val password: String,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val email: String,
    @Column(nullable = false)
    val team: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: MemberRole,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: MemberStatus,
    @Column(nullable = false)
    val lastLoginAt: LocalDateTime

) : BaseEntity() {

    companion object {
        fun of(domain: Member): MemberEntity {
            return MemberEntity(
                loginId = domain.loginId,
                password = domain.password,
                name = domain.name,
                email = domain.email,
                team = domain.team,
                role = domain.role,
                status = domain.status,
                lastLoginAt = domain.lastLoginAt
            )
        }
    }

    override fun toDomain(): Member {
        return Member(
            memberId = id!!,
            loginId = loginId,
            password = password,
            name = name,
            email = email,
            team = team,
            role = role,
            status = status,
            lastLoginAt = lastLoginAt,
            created = created,
            updated = updated
        )
    }
}