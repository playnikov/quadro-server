package com.quadro.notification.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.events.UserUpdatedEvent
import com.quadro.notification.domain.models.User
import com.quadro.notification.domain.models.UserRole
import com.quadro.notification.domain.repositories.UserRepository
import com.quadro.notification.domain.repositories.project.ProjectMemberRepository
import com.quadro.notification.domain.repositories.task.TaskRepository
import com.quadro.notification.domain.services.NotificationService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class UserEventProcessor(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    private val projectMemberRepository: ProjectMemberRepository
) : KoinComponent {
    private val notificationService: NotificationService by inject()

    suspend fun processCreated(event: UserCreatedEvent) {
        val user = User(
            id = UUID.fromString(event.userId),
            role = UserRole.valueOf(event.role),
            isActive = event.isActive
        )

        userRepository.upsert(user)
        notificationService.sendNotification(event)
    }

    suspend fun processUpdated(event: UserUpdatedEvent) {
        val user = User(
            id = UUID.fromString(event.userId),
            role = UserRole.valueOf(event.role),
            isActive = event.isActive
        )

        userRepository.upsert(user)
        notificationService.sendNotification(event)

        if (!event.isActive) {
            taskRepository.clearAssignee(user.id)
            projectMemberRepository.deleteByUserId(user.id)
        }
    }
}