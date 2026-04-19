package com.quadro.project.infrastructure.messaging.processor

import com.quadro.project.domain.models.Company
import com.quadro.project.domain.models.CompanyRole
import com.quadro.project.domain.models.CompanyStatus
import com.quadro.project.domain.repositories.CompanyRepository
import com.quadro.shared.data.messaging.events.CompanyCreatedEvent
import com.quadro.shared.data.messaging.events.CompanyDeletedEvent
import com.quadro.shared.data.messaging.events.CompanyUpdatedEvent
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.time.Instant
import java.util.UUID
import kotlin.time.toKotlinInstant

class CompanyEventProcessor(
    private val companyRepository: CompanyRepository
) {
    suspend fun processCreated(event: CompanyCreatedEvent) {
        val company= Company(
            id = UUID.fromString(event.companyId),
            name = event.name,
            companyStatus = CompanyStatus.valueOf(event.companyStatus),
            projectManagementRole = CompanyRole.valueOf(event.manageProjectRole),
            currentProjects = event.currentProjects,
            maxProjects = event.maxProjects,
            updatedAt = Instant.ofEpochMilli(event.updatedAt).toKotlinInstant()
        )

        companyRepository.upsert(company)
    }

    suspend fun processUpdated(event: CompanyUpdatedEvent) {
        val company= Company(
            id = UUID.fromString(event.companyId),
            name = event.name,
            companyStatus = CompanyStatus.valueOf(event.companyStatus),
            projectManagementRole = CompanyRole.valueOf(event.manageProjectRole),
            currentProjects = event.currentProjects,
            maxProjects = event.maxProjects,
            updatedAt = Instant.ofEpochMilli(event.updatedAt).toKotlinInstant()
        )

        companyRepository.upsert(company)
    }

    suspend fun processDeleted(event: CompanyDeletedEvent) {
        companyRepository.delete(UUID.fromString(event.companyId))
    }
}