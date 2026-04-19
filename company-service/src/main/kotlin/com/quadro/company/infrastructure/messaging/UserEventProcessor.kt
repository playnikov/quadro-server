package com.quadro.company.infrastructure.messaging

import com.quadro.company.domain.models.User
import com.quadro.company.domain.repositories.UserRepository
import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.events.UserUpdatedEvent
import java.util.UUID

class UserEventProcessor(
    private val userRepository: UserRepository
) {
    suspend fun processCreated(event: UserCreatedEvent) {
        val user = User(
            id = UUID.fromString(event.userId),
            isActive = event.isActive
        )
        userRepository.upsert(user)
    }

    suspend fun processUpdated(event: UserUpdatedEvent) {
        val user = User(
            id = UUID.fromString(event.userId),
            isActive = event.isActive
        )
        userRepository.upsert(user)
    }
}