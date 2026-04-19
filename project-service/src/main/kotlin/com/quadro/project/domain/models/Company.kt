package com.quadro.project.domain.models

import java.util.UUID
import kotlin.time.Instant

enum class CompanyStatus { ACTIVE, SUSPENDED, CLOSED, PENDING }

data class Company(
    val id: UUID,
    val name: String,
    val companyStatus: CompanyStatus,
    val projectManagementRole: CompanyRole,
    val currentProjects: Int,
    val maxProjects: Int,
    val updatedAt: Instant,
) {
    fun isActive() = companyStatus == CompanyStatus.ACTIVE
    fun isSuspended() = companyStatus == CompanyStatus.SUSPENDED
}