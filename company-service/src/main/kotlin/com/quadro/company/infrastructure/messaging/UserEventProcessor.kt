package com.quadro.company.infrastructure.messaging

import com.quadro.company.domain.models.User
import com.quadro.company.domain.repositories.UserRepository
import com.quadro.shared.events.UserCreatedEvent
import com.quadro.shared.events.UserDeactivatedEvent
import com.quadro.shared.events.UserUpdatedEvent
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