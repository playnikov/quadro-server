package com.quadro.company.infrastructure.messaging

import com.quadro.company.domain.models.User
import com.quadro.company.domain.repositories.UserRepository
import com.quadro.shared.events.UserEvent
import java.util.UUID

class UserEventProcessor(
    private val userRepository: UserRepository
) {
    suspend fun processCreated(event: UserEvent.Created) {
        val user = User(
            id = UUID.fromString(event.userId),
            email = event.email,
            firstName = event.firstName,
            lastName = event.lastName,
            middleName = event.middleName,
            avatar = event.avatar,
            role = event.role,
            isActive = event.isActive,
            updatedAt = event.updatedAt
        )
        userRepository.upsert(user)
    }

    suspend fun processUpdated(event: UserEvent.Updated) {
        val existing = userRepository.findById(UUID.fromString(event.userId))
        val user = User(
            id = UUID.fromString(event.userId),
            email = event.email,
            firstName = event.firstName,
            lastName = event.lastName,
            middleName = event.middleName,
            avatar = event.avatar,
            role = event.role,
            isActive = event.isActive,
            updatedAt = event.updatedAt
        )
        userRepository.upsert(user)
    }

    suspend fun processDeleted(event: UserEvent.Deleted) {
        userRepository.delete(UUID.fromString(event.userId))
    }
}