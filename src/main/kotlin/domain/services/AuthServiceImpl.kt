package com.quadro.domain.services

import com.quadro.datasource.repositories.UserRepository
import com.quadro.domain.models.AuthResult
import com.quadro.domain.models.User
import com.quadro.domain.models.UserCreate
import com.quadro.domain.models.UserLogin
import com.quadro.security.JwtTokenService
import com.quadro.security.PasswordEncoder
import org.slf4j.LoggerFactory
import java.util.*

class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val jwtTokenService: JwtTokenService,
    private val passwordEncoder: PasswordEncoder
) : AuthService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun register(request: UserCreate): Result<AuthResult> {
        return try {
            validateRegistration(request)

            if (userRepository.existsByEmail(request.email)) {
                return Result.failure(Exception("Email already registered"))
            }
            if (userRepository.existsByUsername(request.username)) {
                return Result.failure(Exception("Username already taken"))
            }

            val user = User(
                id = UUID.randomUUID(),
                email = request.email.lowercase(),
                username = request.username.lowercase(),
                passwordHash = passwordEncoder.encode(request.password),
                firstName = request.firstName,
                lastName = request.lastName,
                role = request.role,
                isEmailVerified = false,
                isActive = true,
                avatar = null,
            )

            val createdUser = userRepository.create(user)
            logger.info("User registered: ${createdUser.email}")

            val accessToken = jwtTokenService.generateAccessToken(createdUser)
            val refreshToken = jwtTokenService.generateRefreshToken(createdUser)

            Result.success(
                AuthResult(
                    token = accessToken,
                    refreshToken = refreshToken
                )
            )
        } catch (e: Exception) {
            logger.error("Registration failed", e)
            Result.failure(e)
        }
    }

    override suspend fun login(request: UserLogin): Result<AuthResult> {
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

            if (!passwordEncoder.verifyPassword(request.password, user.passwordHash)) {
                return Result.failure(Exception("Invalid login or password"))
            }

            val accessToken = jwtTokenService.generateAccessToken(user)
            val refreshToken = jwtTokenService.generateRefreshToken(user)


            Result.success(
                AuthResult(
                    token = accessToken,
                    refreshToken = refreshToken
                )
            )
        } catch (e: Exception) {
            logger.error("Login failed", e)
            Result.failure(e)
        }
    }

    override suspend fun validateToken(token: String): Result<User?> {
        return try {
            val userId =
                jwtTokenService.validateToken(token).userId ?: return Result.failure(Exception("Invalid token"))

            val user = userRepository.findById(userId) ?: return Result.failure(Exception("User not found"))

            if (!user.isActive) {
                return Result.failure(Exception("User is deactivated"))
            }

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
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