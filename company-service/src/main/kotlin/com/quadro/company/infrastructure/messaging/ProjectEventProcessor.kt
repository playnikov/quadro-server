package com.quadro.company.infrastructure.messaging

import com.quadro.company.domain.models.User
import com.quadro.company.domain.repositories.CompanyRepository
import com.quadro.company.domain.repositories.UserRepository
import com.quadro.shared.data.messaging.events.ProjectArchivedEvent
import com.quadro.shared.data.messaging.events.ProjectCreatedEvent
import com.quadro.shared.data.messaging.events.ProjectDeletedEvent
import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.events.UserUpdatedEvent
import java.util.UUID

class ProjectEventProcessor(
    private val companyRepository: CompanyRepository
) {
    suspend fun processCreated(event: ProjectCreatedEvent) {
        companyRepository.incrementProjectCount(UUID.fromString(event.companyId))
    }

    suspend fun processDeleted(event: ProjectDeletedEvent) {
        companyRepository.decrementProjectCount(UUID.fromString(event.companyId))
    }

    suspend fun processArchived(event: ProjectArchivedEvent) {
        companyRepository.decrementProjectCount(UUID.fromString(event.companyId))
    }
}