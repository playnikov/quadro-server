package com.quadro.project.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class CompanyRole {
    GUEST, MEMBER, MANAGER, ADMIN, OWNER;

    fun isAtLeast(other: CompanyRole) = ordinal >= other.ordinal
    fun isHigherThan(other: CompanyRole) = ordinal > other.ordinal
}

data class CompanyMember(
    val id: UUID,
    val companyId: UUID,
    val userId: UUID,
    val role: CompanyRole
)