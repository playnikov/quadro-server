package com.quadro.project.infrastructure.messaging.processor

import com.quadro.project.domain.models.CompanyMember
import com.quadro.project.domain.models.CompanyRole
import com.quadro.project.domain.repositories.CompanyMemberRepository
import com.quadro.shared.data.messaging.events.CompanyCreatedEvent
import com.quadro.shared.data.messaging.events.CompanyDeletedEvent
import com.quadro.shared.data.messaging.events.CompanyMemberAddedEvent
import com.quadro.shared.data.messaging.events.CompanyMemberRemovedEvent
import com.quadro.shared.data.messaging.events.CompanyMemberRoleUpdatedEvent
import com.quadro.shared.data.messaging.events.CompanyUpdatedEvent
import java.time.Instant
import java.util.UUID
import kotlin.time.toKotlinInstant

class CompanyMemberEventProcessor(
    private val companyMemberRepository: CompanyMemberRepository
) {
    suspend fun processCreated(event: CompanyMemberAddedEvent) {
        val member = CompanyMember(
            id = UUID.fromString(event.memberId),
            companyId = UUID.fromString(event.companyId),
            userId = UUID.fromString(event.userId),
            role = CompanyRole.valueOf(event.role)
        )

        companyMemberRepository.upsert(member)
    }

    suspend fun processUpdated(event: CompanyMemberRoleUpdatedEvent) {
        val member = CompanyMember(
            id = UUID.fromString(event.memberId),
            companyId = UUID.fromString(event.companyId),
            userId = UUID.fromString(event.userId),
            role = CompanyRole.valueOf(event.role)
        )

        companyMemberRepository.upsert(member)
    }

    suspend fun processDeleted(event: CompanyMemberRemovedEvent) {
        companyMemberRepository.delete(UUID.fromString(event.memberId))
    }
}