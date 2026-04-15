package com.quadro.team.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.CompanyCreatedEvent
import com.quadro.shared.data.messaging.events.CompanyDeletedEvent
import com.quadro.shared.data.messaging.events.CompanyUpdatedEvent
import com.quadro.team.domain.models.Company
import com.quadro.team.domain.models.CompanyRole
import com.quadro.team.domain.models.CompanyStatus
import com.quadro.team.domain.repositories.CompanyRepository
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
            createRole = CompanyRole.valueOf(event.createTeamRole),
            updatedAt = Instant.ofEpochMilli(event.updatedAt).toKotlinInstant()
        )

        companyRepository.upsert(company)
    }

    suspend fun processUpdated(event: CompanyUpdatedEvent) {
        val company= Company(
            id = UUID.fromString(event.companyId),
            name = event.name,
            companyStatus = CompanyStatus.valueOf(event.companyStatus),
            createRole = CompanyRole.valueOf(event.createTeamRole),
            updatedAt = Instant.ofEpochMilli(event.updatedAt).toKotlinInstant()
        )

        companyRepository.upsert(company)
    }

    suspend fun processDeleted(event: CompanyDeletedEvent) {
        companyRepository.delete(UUID.fromString(event.companyId))
    }
}