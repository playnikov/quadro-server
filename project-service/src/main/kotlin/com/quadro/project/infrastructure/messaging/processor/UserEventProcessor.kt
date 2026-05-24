package com.quadro.project.infrastructure.messaging.processor

import com.quadro.project.domain.models.User
import com.quadro.project.domain.models.UserRole
import com.quadro.project.domain.repositories.UserRepository
import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.events.UserUpdatedEvent
import java.util.UUID

class UserEventProcessor(
    private val userRepository: UserRepository
) {
    suspend fun processCreated(event: UserCreatedEvent) {
        val user = User(
            id = UUID.fromString(event.userId),
            email = event.email,
            lastName = event.lastName,
            firstName = event.firstName,
            middleName = event.middleName,
            role = UserRole.valueOf(event.role),
            isActive = event.isActive
        )
        userRepository.upsert(user)
    }

    suspend fun processUpdated(event: UserUpdatedEvent) {
        val user = User(
            id = UUID.fromString(event.userId),
            role = UserRole.valueOf(event.role),
            email = event.email,
            lastName = event.lastName,
            firstName = event.firstName,
            middleName = event.middleName,
            isActive = event.isActive
        )
        userRepository.upsert(user)
    }
}