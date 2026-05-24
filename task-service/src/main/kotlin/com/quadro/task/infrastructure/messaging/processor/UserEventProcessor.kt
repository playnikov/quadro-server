package com.quadro.task.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.events.UserUpdatedEvent
import com.quadro.task.domain.models.User
import com.quadro.task.domain.models.UserRole
import com.quadro.task.domain.repositories.UserRepository
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import java.util.UUID

class UserEventProcessor(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
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
            email = event.email,
            lastName = event.lastName,
            firstName = event.firstName,
            middleName = event.middleName,
            role = UserRole.valueOf(event.role),
            isActive = event.isActive
        )

        userRepository.upsert(user)

        if (!event.isActive) {
            taskRepository.clearAssignee(user.id)
        }
    }
}