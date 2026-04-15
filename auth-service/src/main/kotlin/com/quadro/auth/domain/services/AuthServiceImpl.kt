package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.AuthResult
import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserLogin
import com.quadro.auth.domain.models.UserResult
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.domain.repositories.UserRepository
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
//    private val sessionCache: SessionCache,
    private val eventProducer: EventProducer,
) : AuthService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun register(
        request: UserCreate,
        ipAddress: String?
    ): AuthResult {
        validateRegistration(request)

        if (userRepository.existsByEmail(request.email)) {
            throw DomainException.AlreadyExists("Email '${request.email}'")
        }
        if (userRepository.existsByUsername(request.username)) {
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

        val createdUser = userRepository.create(user)
        logger.info("User registered: ${createdUser.email}")

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
                isActive = createdUser.isActive
            )
        )

        val accessToken = jwtProvider.generateAccessToken(createdUser)
        val refreshToken = jwtProvider.generateRefreshToken(createdUser)

        return AuthResult(
            token = accessToken,
            refreshToken = refreshToken,
            userInfo = UserResult.fromUser(createdUser)
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
            throw DomainException.ValidationError("Invalid login or password")
        }
        if (!user.isActive) {
            throw DomainException.BusinessRule("User is deactivated")
        }

        val accessToken = jwtProvider.generateAccessToken(user)
        val refreshToken = jwtProvider.generateRefreshToken(user)

        return AuthResult(
            token = accessToken,
            refreshToken = refreshToken,
            userInfo = UserResult.fromUser(user)
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
            userInfo = UserResult.fromUser(user)
        )
    }


    override suspend fun validateToken(token: String): UserResult {
        val validation = jwtValidator.validateToken(token)
        if (!validation.isValid || validation.userId == null) {
            throw DomainException.ValidationError(validation.error ?: "Invalid token")
        }

        val user = userRepository.findById(validation.userId!!)
            ?: throw DomainException.NotFound("User", validation.userId.toString())
        if (!user.isActive) {
            throw DomainException.BusinessRule("User is deactivated")
        }

        return UserResult.fromUser(user)
    }

    override suspend fun changePassword(
        userId: UUID,
        currentPassword: String,
        newPassword: String
    ) {
        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", userId.toString())
        if (!user.isActive) {
            throw DomainException.BusinessRule("User is deactivated")
        }
        if (!passwordEncoder.verify(currentPassword, user.passwordHash)) {
            throw DomainException.ValidationError("Current password is incorrect")
        }
        validatePassword(newPassword)

        val updatedUser = user.copy(
            passwordHash = passwordEncoder.encode(newPassword),
            updatedAt = Clock.System.now()
        )
        userRepository.update(updatedUser)

        logger.info("Password changed for user: ${user.email}")
    }

    override suspend fun forgotPassword(email: String) {
        TODO("Not yet implemented")
    }

    override suspend fun resetPassword(token: String, newPassword: String) {
        val validation = jwtValidator.validateToken(token)
        if (!validation.isValid || validation.userId == null) {
            throw DomainException.ValidationError("Invalid or expired token")
        }

        val user = userRepository.findById(validation.userId!!)
            ?: throw DomainException.NotFound("User", validation.userId.toString())

        validatePassword(newPassword)

        val updatedUser = user.copy(
            passwordHash = passwordEncoder.encode(newPassword),
            updatedAt = Clock.System.now()
        )
        userRepository.update(updatedUser)

        logger.info("Password reset for user: ${user.email}")
    }

    override suspend fun verifyEmail(token: String) {
        val validation = jwtValidator.validateToken(token)
        if (!validation.isValid || validation.userId == null) {
            throw DomainException.ValidationError("Invalid or expired token")
        }

        val user = userRepository.findById(validation.userId!!)
            ?: throw DomainException.NotFound("User", validation.userId.toString())

        val updatedUser = user.copy(
            isEmailVerified = true,
            updatedAt = Clock.System.now()
        )
        userRepository.update(updatedUser)

        logger.info("Email verified for user: ${user.email}")
    }

    override suspend fun logout(userId: UUID) {
        TODO("Not yet implemented")
    }

    override suspend fun getUser(userId: UUID): UserResult {
        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", userId.toString())
        return UserResult.fromUser(user)
    }

    private fun validateRegistration(request: UserCreate) {
        validateEmail(request.email)
        validateUsername(request.username)
        validatePassword(request.password)
    }

    private fun validateEmail(email: String) {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        if (!email.matches(emailRegex.toRegex())) {
            throw Exception("Invalid email format")
        }
    }

    private fun validateUsername(username: String) {
        if (username.length !in 3..50) {
            throw Exception("Username must be between 3 and 50 characters")
        }
        if (!username.matches(Regex("^[a-zA-Z0-9._-]+$"))) {
            throw Exception("Username contains invalid characters")
        }
    }

    private fun validatePassword(password: String) {
        if (password.length < 8) {
            throw Exception("Password must be at least 8 characters")
        }
        if (!password.any { it.isDigit() }) {
            throw Exception("Password must contain at least one digit")
        }
        if (!password.any { it.isLetter() }) {
            throw Exception("Password must contain at least one letter")
        }
    }
}