package com.quadro.notification.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.TeamCreatedEvent
import com.quadro.shared.data.messaging.events.TeamDeletedEvent
import com.quadro.shared.data.messaging.events.TeamMemberAddedEvent
import com.quadro.shared.data.messaging.events.TeamMemberRemovedEvent
import com.quadro.shared.data.messaging.events.TeamMemberUpdatedEvent
import com.quadro.shared.data.messaging.events.TeamProjectAssignedEvent
import com.quadro.shared.data.messaging.events.TeamProjectUnassignedEvent
import com.quadro.shared.data.messaging.events.TeamProjectUpdatedEvent
import com.quadro.shared.data.messaging.events.TeamUpdatedEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class TeamEventProcessor(
) : KoinComponent {

    suspend fun processTeamCreated(event: TeamCreatedEvent) {

    }

    suspend fun processTeamUpdated(event: TeamUpdatedEvent) {

    }

    suspend fun processTeamDeleted(event: TeamDeletedEvent) {

    }

    suspend fun processTeamMemberAdded(event: TeamMemberAddedEvent) {

    }

    suspend fun processTeamMemberUpdated(event: TeamMemberUpdatedEvent) {

    }

    suspend fun processTeamMemberRemoved(event: TeamMemberRemovedEvent) {

    }

    suspend fun processTeamProjectAssigned(event: TeamProjectAssignedEvent) {

    }

    suspend fun processTeamProjectUpdated(event: TeamProjectUpdatedEvent) {

    }

    suspend fun processTeamProjectUnassigned(event: TeamProjectUnassignedEvent) {

    }
}