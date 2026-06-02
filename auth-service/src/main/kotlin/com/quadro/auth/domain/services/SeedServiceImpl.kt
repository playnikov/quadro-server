package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.infrastructure.security.PasswordEncoder
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.dto.DomainException
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class SeedServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val eventProducer: EventProducer
) : SeedService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun createSuperAdminIfNotExists(credentials: UserCreate): Boolean {

        val existingSuperAdmin = userRepository.getAll()
            .firstOrNull { it.role == UserRole.SUPER_ADMIN }

        if (existingSuperAdmin != null) {
            logger.info("Super Admin user already exists with email: ${existingSuperAdmin.email}")
            return false
        }

        if (userRepository.existsByEmail(credentials.email)) {
            throw DomainException.AlreadyExists("Email '${credentials.email}' is already in use")
        }

        if (userRepository.existsByUsername(credentials.username)) {
            throw DomainException.AlreadyExists("Username '${credentials.username}' is already in use")
        }

        val now = Clock.System.now()
        val user = User(
            id = UUID.randomUUID(),
            email = credentials.email,
            username = credentials.username,
            passwordHash = passwordEncoder.encode(credentials.password),
            firstName = credentials.firstName,
            lastName = credentials.lastName,
            middleName = credentials.middleName,
            role = UserRole.SUPER_ADMIN,
            isActive = true,
            isEmailVerified = true,
            isNeedChangePassword = credentials.isNeedChangePassword == true,
            createdAt = now,
            updatedAt = now
        )

        val createdUser = userRepository.upsert(user)

        eventProducer.publish(
            topic = KafkaTopics.USER_CREATED,
            key = createdUser.id.toString(),
            event = UserCreatedEvent(
                userId = createdUser.id.toString(),
                email = createdUser.email,
                firstName = createdUser.firstName,
                lastName = createdUser.lastName,
                middleName = createdUser.middleName ?: "",
                avatar = createdUser.avatarUrl ?: "",
                role = createdUser.role.name,
                isActive = createdUser.isActive
            )
        )

        logger.info("Super Admin user created: ${createdUser.email}")
        return true
    }
}
