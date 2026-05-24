package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.AuthResult
import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserLogin
import com.quadro.auth.domain.models.UserResponse
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.domain.utils.validateEmail
import com.quadro.auth.domain.utils.validatePassword
import com.quadro.auth.domain.utils.validateUsername
import com.quadro.auth.infrastructure.security.JwtProvider
import com.quadro.auth.infrastructure.security.PasswordEncoder
import com.quadro.shared.dto.DomainException
import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.security.JwtValidator
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val jwtValidator: JwtValidator,
    private val eventProducer: EventProducer,
) : AuthService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun register(
        request: UserCreate,
        ipAddress: String?
    ): AuthResult {
        validateRegistration(request)

        if (userRepository.existsByEmail(request.email)) {
            logger.warn("Registration attempt with existing email: ${request.email}, IP: $ipAddress")
            throw DomainException.AlreadyExists("Email '${request.email}'")
        }
        if (userRepository.existsByUsername(request.username)) {
            logger.warn("Registration attempt with existing username: ${request.username}, IP: $ipAddress")
            throw DomainException.AlreadyExists("Username '${request.username}'")
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
            createdAt = now,
            updatedAt = now
        )

        val createdUser = userRepository.upsert(user)
        logger.info("User registered: ${createdUser.email}, IP: $ipAddress")

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

        val accessToken = jwtProvider.generateAccessToken(createdUser)
        val refreshToken = jwtProvider.generateRefreshToken(createdUser)

        return AuthResult(
            token = accessToken,
            refreshToken = refreshToken,
            userInfo = UserResponse.from(createdUser)
        )
    }

    override suspend fun login(
        request: UserLogin,
        ipAddress: String?,
        userAgent: String?
    ): AuthResult {
        val user = if (request.name.contains('@') && request.name.contains(".")) {
            userRepository.findByEmail(request.name)
        } else {
            userRepository.findByUsername(request.name)
        }

        if (user == null || !passwordEncoder.verify(request.password, user.passwordHash)) {
            logger.warn("Failed login attempt for: ${request.name}, IP: $ipAddress, User-Agent: $userAgent")
            throw DomainException.ValidationError("Invalid login or password")
        }
        if (!user.isActive) {
            logger.warn("Login attempt for deactivated user: ${user.email}, IP: $ipAddress, User-Agent: $userAgent")
            throw DomainException.BusinessRule("User is deactivated")
        }

        val accessToken = jwtProvider.generateAccessToken(user)
        val refreshToken = jwtProvider.generateRefreshToken(user)

        logger.info("Successful login for user: ${user.email}, IP: $ipAddress, User-Agent: $userAgent")

        userRepository.upsert(user.copy(
            lastLoginAt = Clock.System.now()
        ))
        return AuthResult(
            token = accessToken,
            refreshToken = refreshToken,
            userInfo = UserResponse.from(user)
        )
    }

    override suspend fun refreshToken(refreshToken: String): AuthResult {
        val validation = jwtValidator.validateToken(refreshToken)
        if (!validation.isValid || validation.userId == null) {
            throw DomainException.ValidationError("Invalid refresh token")
        }

        val user = userRepository.findById(validation.userId!!)
            ?: throw DomainException.NotFound("User", validation.userId.toString())
        if (!user.isActive) {
            throw DomainException.BusinessRule("User is deactivated")
        }

        val newAccessToken = jwtProvider.generateAccessToken(user)
        val newRefreshToken = jwtProvider.generateRefreshToken(user)

        return AuthResult(
            token = newAccessToken,
            refreshToken = newRefreshToken,
            userInfo = UserResponse.from(user)
        )
    }


    override suspend fun validateToken(token: String): UserResponse {
        val validation = jwtValidator.validateToken(token)
        if (!validation.isValid || validation.userId == null) {
            throw DomainException.ValidationError(validation.error ?: "Invalid token")
        }

        val user = userRepository.findById(validation.userId!!)
            ?: throw DomainException.NotFound("User", validation.userId.toString())
        if (!user.isActive) {
            throw DomainException.BusinessRule("User is deactivated")
        }

        return UserResponse.from(user)
    }

    override suspend fun changePassword(
        userId: UUID,
        currentPassword: String,
        newPassword: String
    ) {
        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", userId.toString())
        if (!user.isActive) {
            logger.warn("Password change attempt for deactivated user: ${user.email}")
            throw DomainException.BusinessRule("User is deactivated")
        }
        if (!passwordEncoder.verify(currentPassword, user.passwordHash)) {
            logger.warn("Password change failed: incorrect current password for user: ${user.email}")
            throw DomainException.ValidationError("Current password is incorrect")
        }
        validatePassword(newPassword)

        val updatedUser = user.copy(
            passwordHash = passwordEncoder.encode(newPassword),
            isNeedChangePassword = false,
            updatedAt = Clock.System.now()
        )
        userRepository.upsert(updatedUser)

        logger.info("Password changed successfully for user: ${user.email}")
    }

    override suspend fun changePassword(userId: UUID, newPassword: String) {
        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", userId.toString())
        if (passwordEncoder.verify(newPassword, user.passwordHash)) {
            logger.warn("Password change failed for user: ${user.email}")
            throw DomainException.ValidationError("The new password cannot be equal to the current one.t")
        }
        validatePassword(newPassword)

        val updatedUser = user.copy(
            passwordHash = passwordEncoder.encode(newPassword),
            isNeedChangePassword = false,
            updatedAt = Clock.System.now()
        )
        userRepository.upsert(updatedUser)

        logger.info("Password changed successfully for user: ${user.email}")
    }

    override suspend fun forgotPassword(email: String) {
        logger.info("Password reset requested for email: $email")
        val user = userRepository.findByEmail(email)
        if (user != null && user.isActive) {
            // Здесь будет реализация отправки email с токеном сброса пароля
            logger.info("Password reset email sent to: $email")
        } else {
            logger.warn("Password reset requested for non-existent or deactivated user: $email")
        }
        // Возвращаем успех даже если пользователь не найден для предотвращения перебора email
    }

    override suspend fun resetPassword(token: String, newPassword: String) {
        val validation = jwtValidator.validateToken(token)
        if (!validation.isValid || validation.userId == null) {
            logger.warn("Password reset failed: invalid or expired token")
            throw DomainException.ValidationError("Invalid or expired token")
        }

        val user = userRepository.findById(validation.userId!!)
            ?: throw DomainException.NotFound("User", validation.userId.toString())

        validatePassword(newPassword)

        val updatedUser = user.copy(
            passwordHash = passwordEncoder.encode(newPassword),
            updatedAt = Clock.System.now()
        )
        userRepository.upsert(updatedUser)

        logger.info("Password reset successful for user: ${user.email}")
    }

    override suspend fun verifyEmail(token: String) {
        val validation = jwtValidator.validateToken(token)
        if (!validation.isValid || validation.userId == null) {
            logger.warn("Email verification failed: invalid or expired token")
            throw DomainException.ValidationError("Invalid or expired token")
        }

        val user = userRepository.findById(validation.userId!!)
            ?: throw DomainException.NotFound("User", validation.userId.toString())

        val updatedUser = user.copy(
            isEmailVerified = true,
            updatedAt = Clock.System.now()
        )
        userRepository.upsert(updatedUser)

        logger.info("Email verified for user: ${user.email}")
    }

    private fun validateRegistration(request: UserCreate) {
        validateEmail(request.email)
        validateUsername(request.username)
        validatePassword(request.password)
    }
}