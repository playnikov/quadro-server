package com.quadro.team.domain.models

import java.util.UUID
import kotlin.time.Instant

enum class CompanyStatus { ACTIVE, SUSPENDED, CLOSED, PENDING }

data class Company(
    val id: UUID,
    val companyStatus: CompanyStatus,
    val teamManagementRole: CompanyRole,
    val updatedAt: Instant,
) {
    fun isActive() = companyStatus == CompanyStatus.ACTIVE
    fun isSuspended() = companyStatus == CompanyStatus.SUSPENDED
}