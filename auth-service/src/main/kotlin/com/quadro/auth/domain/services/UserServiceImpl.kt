package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.infrastructure.security.PasswordEncoder
import com.quadro.auth.presentation.models.UpdateAdminUserRequest
import com.quadro.auth.utils.validateEmail
import com.quadro.auth.utils.validatePassword
import com.quadro.auth.utils.validateUsername
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.events.UserUpdatedEvent
import com.quadro.shared.dto.DomainException
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val eventProducer: EventProducer
): UserService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun getUserById(id: UUID): User {
        val user = userRepository.findById(id)
            ?: throw DomainException.NotFound("User", id.toString())
        return user
    }

    override suspend fun getAllUsers(requesterId: UUID): List<User> {
        val requester = userRepository.findById(requesterId)
            ?: throw DomainException.NotFound("User", requesterId.toString())
        if (!requester.role.isAdmin()) {
            throw DomainException.AccessDenied()
        }
        return userRepository.getAll()
    }

    override suspend fun getUserByUsername(username: String): User {
        val user = userRepository.findByUsername(username)
            ?: throw DomainException.NotFound("User", username)
        return user
    }

    override suspend fun getUserByEmail(email: String): User {
        val user = userRepository.findByEmail(email)
            ?: throw DomainException.NotFound("User", email)
        return user
    }

    override suspend fun getUsersByIds(userIds: List<UUID>): List<User> {
        return userRepository.getByIds(userIds)
    }

    override suspend fun updateUserByAdmin(requesterId: UUID, userId: UUID, request: UpdateAdminUserRequest): User {
        val requester = userRepository.findById(requesterId)
            ?: throw DomainException.NotFound("User", requesterId.toString())

        if (!requester.role.isAdmin() || requesterId == userId || (!requester.role.isSuperAdmin() && request.role != null)) {
            throw DomainException.AccessDenied()
        }

        request.username?.let { validateUsername(it) }
        request.email?.let { validateEmail(it) }
        request.password?.let { validatePassword(it) }

        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", userId.toString())

        val updateUser = user.copy(
            username = request.username ?: user.username,
            email = request.email ?: user.email,
            passwordHash = request.password?.let { passwordEncoder.encode(it) } ?: user.passwordHash,
            isNeedChangePassword = request.password?.let { true } ?: false,
            firstName = request.firstName ?: user.firstName,
            lastName = request.lastName ?: user.lastName,
            middleName = request.middleName ?: user.middleName,
            role = request.role ?: user.role,
            isActive = request.isActive ?: user.isActive,
            updatedAt = Clock.System.now()
        )

        eventProducer.publish(
            topic = KafkaTopics.USER_UPDATED,
            key = updateUser.id.toString(),
            event = UserUpdatedEvent(
                userId = updateUser.id.toString(),
                email = updateUser.email,
                firstName = updateUser.firstName,
                lastName = updateUser.lastName,
                middleName = updateUser.middleName ?: "",
                avatar = updateUser.avatarUrl ?: "",
                isActive = updateUser.isActive,
                role = updateUser.role.name,
                updatedAt = updateUser.updatedAt.toEpochMilliseconds()
            )
        )

        return userRepository.upsert(updateUser)
    }

    override suspend fun adminCreateUser(
        requesterId: UUID,
        request: UserCreate
    ): User {
        validateRegistration(request)

        val requester = userRepository.findById(requesterId)
            ?: throw DomainException.NotFound("User", requesterId.toString())

        if (!requester.role.isAdmin()) {
            throw DomainException.AccessDenied()
        }

        val now = Clock.System.now()
        val user = User(
            id = UUID.randomUUID(),
            email = request.email,
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            firstName = request.firstName,
            lastName = request.lastName,
            middleName = request.middleName,
            role = UserRole.USER,
            isNeedChangePassword = true,
            createdAt = now,
            updatedAt = now
        )

        val createdUser = userRepository.upsert(user)
        logger.info("User registered: ${createdUser.email} by ${requester.email}:${requester.role}")

        eventProducer.publish(
            topic = KafkaTopics.USER_CREATED,
            key = createdUser.id.toString(),
            event = UserCreatedEvent(
                userId = createdUser.id.toString(),
                email = createdUser.email,
                firstName = createdUser.firstName,
                lastName = createdUser.lastName,
                middleName = createdUser.middleName,
                avatar = createdUser.avatarUrl,
                role = createdUser.role.name,
                isActive = createdUser.isActive
            )
        )

        return createdUser
    }

    private fun validateRegistration(request: UserCreate) {
        validateEmail(request.email)
        validateUsername(request.username)
        validatePassword(request.password)
    }
}