package com.quadro.auth.domain.services

import com.quadro.auth.config.AppConfig
import com.quadro.auth.domain.models.AuthResult
import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserLogin
import com.quadro.auth.domain.models.UserResult
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.infrastructure.security.JwtProvider
import com.quadro.auth.infrastructure.security.PasswordEncoder
import com.quadro.shared.security.JwtValidator
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val jwtValidator: JwtValidator,
//    private val sessionCache: SessionCache,
//    private val eventProducer: UserEventProducer,
) : AuthService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun register(
        request: UserCreate,
        ipAddress: String?
    ): Result<AuthResult> {
        return try {
            validateRegistration(request)
            if (userRepository.existsByEmail(request.email)) {
                return Result.failure(Exception("Email already registered"))
            }
            if (userRepository.existsByUsername(request.username)) {
                return Result.failure(Exception("Username already taken"))
            }

            val now = Clock.System.now()
            val user = User(
                id = UUID.randomUUID(),
                email = request.email.lowercase(),
                username = request.username.lowercase(),
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
            val accessToken = jwtProvider.generateAccessToken(createdUser)
            val refreshToken = jwtProvider.generateRefreshToken(createdUser)

            Result.success(
                AuthResult(
                    token = accessToken,
                    refreshToken = refreshToken,
                    userInfo = UserResult.fromUser(createdUser)
                )
            )
        } catch (e: Exception) {
            logger.error("Registration failed", e)
            Result.failure(e)
        }
    }

    override suspend fun login(
        request: UserLogin,
        ipAddress: String?,
        userAgent: String?
    ): Result<AuthResult> {
        return try {
            val user = request.username?.let {
                userRepository.findByUsername(request.username)
                    ?: return Result.failure(Exception("Invalid credentials"))
            } ?: request.email?.let {
                userRepository.findByEmail(request.email) ?: return Result.failure(Exception("Invalid credentials"))
            } ?: return Result.failure(Exception("Invalid credentials"))

            if (!user.isActive) {
                return Result.failure(Exception("User is deactivated"))
            }

            if (!passwordEncoder.verify(request.password, user.passwordHash)) {
                return Result.failure(Exception("Invalid login or password"))
            }

            val accessToken = jwtProvider.generateAccessToken(user)
            val refreshToken = jwtProvider.generateRefreshToken(user)

            Result.success(
                AuthResult(
                    token = accessToken,
                    refreshToken = refreshToken,
                    userInfo = UserResult.fromUser(user)
                )
            )
        } catch (e: Exception) {
            logger.error("Login failed", e)
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthResult> {
        return try {
            val validation = jwtValidator.validateToken(refreshToken)
            if (!validation.isValid || validation.userId == null) {
                return Result.failure(Exception("Invalid refresh token"))
            }

            val user = userRepository.findById(validation.userId!!)
                ?: return Result.failure(Exception("User not found"))

            if (!user.isActive) {
                return Result.failure(Exception("User is deactivated"))
            }

            val newAccessToken = jwtProvider.generateAccessToken(user)
            val newRefreshToken = jwtProvider.generateRefreshToken(user)

            Result.success(
                AuthResult(
                    token = newAccessToken,
                    refreshToken = newRefreshToken,
                    userInfo = UserResult.fromUser(user)
                )
            )
        } catch (e: Exception) {
            logger.error("Token refresh failed", e)
            Result.failure(e)
        }
    }

    override suspend fun validateToken(token: String): Result<User> {
        return try {
            val validation  = jwtValidator.validateToken(token)
            if (!validation.isValid || validation.userId == null) {
                return Result.failure(Exception(validation.error ?: "Invalid token"))
            }

            val user = userRepository.findById(validation.userId!!)
                ?: return Result.failure(Exception("User not found"))

            if (!user.isActive) {
                return Result.failure(Exception("User is deactivated"))
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(
        userId: UUID,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val user = userRepository.findById(userId)
                ?: return Result.failure(Exception("User not found"))

            if (!user.isActive) {
                return Result.failure(Exception("User is deactivated"))
            }

            if (!passwordEncoder.verify(currentPassword, user.passwordHash)) {
                return Result.failure(Exception("Current password is incorrect"))
            }

            validatePassword(newPassword)

            val updatedUser = user.copy(
                passwordHash = passwordEncoder.encode(newPassword),
                updatedAt = Clock.System.now()
            )
            userRepository.update(updatedUser)

            logger.info("Password changed for user: ${user.email}")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Password change failed", e)
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> {
        return try {
            val validation = jwtValidator.validateToken(token)

            if (!validation.isValid || validation.userId == null) {
                return Result.failure(Exception("Invalid or expired token"))
            }

            val user = userRepository.findById(validation.userId!!)
                ?: return Result.failure(Exception("User not found"))

            validatePassword(newPassword)

            val updatedUser = user.copy(
                passwordHash = passwordEncoder.encode(newPassword),
                updatedAt = Clock.System.now()
            )
            userRepository.update(updatedUser)

            logger.info("Password reset for user: ${user.email}")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Password reset failed", e)
            Result.failure(e)
        }
    }

    override suspend fun verifyEmail(token: String): Result<Unit> {
        return try {
            val validation = jwtValidator.validateToken(token)
            if (!validation.isValid || validation.userId == null) {
                return Result.failure(Exception("Invalid or expired token"))
            }

            val user = userRepository.findById(validation.userId!!)
                ?: return Result.failure(Exception("User not found"))

            val updatedUser = user.copy(
                isEmailVerified = true,
                updatedAt = Clock.System.now()
            )
            userRepository.update(updatedUser)

            logger.info("Email verified for user: ${user.email}")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Email verification failed", e)
            Result.failure(e)
        }
    }

    override suspend fun logout(userId: UUID): Result<Unit> {
        TODO("Not yet implemented")
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