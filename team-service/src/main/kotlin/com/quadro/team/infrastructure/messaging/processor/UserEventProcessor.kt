package com.quadro.team.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.events.UserUpdatedEvent
import com.quadro.team.domain.models.User
import com.quadro.team.domain.repositories.UserRepository
import java.util.UUID

class UserEventProcessor(
    private val userRepository: UserRepository
) {
    suspend fun processCreated(event: UserCreatedEvent) {
        val user = User(
            id = UUID.fromString(event.userId),
            email = event.email,
            firstName = event.firstName,
            lastName = event.lastName,
            middleName = event.middleName,
            avatar = event.avatar,
            isActive = event.isActive
        )
        userRepository.upsert(user)
    }

    suspend fun processUpdated(event: UserUpdatedEvent) {
        val user = User(
            id = UUID.fromString(event.userId),
            email = event.email,
            firstName = event.firstName,
            lastName = event.lastName,
            middleName = event.middleName,
            avatar = event.avatar,
            isActive = event.isActive
        )
        userRepository.upsert(user)
    }
}