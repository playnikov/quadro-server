package com.quadro.team.domain.models

import java.util.UUID
import kotlin.time.Instant

enum class CompanyStatus { ACTIVE, SUSPENDED, CLOSED, PENDING }

data class Company(
    val id: UUID,
    val name: String,
    val companyStatus: CompanyStatus,
    val createRole: CompanyRole,
    val updatedAt: Instant,
) {
    fun isActive() = companyStatus == CompanyStatus.ACTIVE
    fun isSuspended() = companyStatus == CompanyStatus.SUSPENDED
}

data class CompanySettings(
    val teamCreationRole: CompanyRole = CompanyRole.MANAGER,
)