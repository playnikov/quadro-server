package com.quadro.auth

import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserLogin
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.domain.services.AuthService
import com.quadro.auth.domain.services.AuthServiceImpl
import com.quadro.auth.infrastructure.security.JwtProvider
import com.quadro.auth.infrastructure.security.PasswordEncoder
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.JwtValidator
import com.quadro.shared.security.TokenValidationResult
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.*
import kotlin.time.Clock

class AuthServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var jwtValidator: JwtValidator
    private lateinit var jwtProvider: JwtProvider
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authService: AuthService
    private lateinit var eventProducer: EventProducer

    private val testUserId = UUID.randomUUID()
    private val testEmail = "test@example.com"
    private val testUsername = "testuser"
    private val testPassword = "Password123"
    private val testLastName = "Test"
    private val testFirstName = "Test"
    private val testMiddleName = "Test"
    private lateinit var testPasswordHash: String
    private val testAccessToken = "access.token.string"
    private val testRefreshToken = "refresh.token.string"

    @Before
    fun setUp() {
        userRepository = mockk(relaxed = true)
        jwtProvider = mockk(relaxed = true)
        jwtValidator = mockk(relaxed = true)
        passwordEncoder = mockk(relaxed = true)
        eventProducer = mockk(relaxed = true)

        testPasswordHash = passwordEncoder.encode(testPassword)

        authService = AuthServiceImpl(
            userRepository = userRepository,
            passwordEncoder = passwordEncoder,
            jwtProvider = jwtProvider,
            jwtValidator = jwtValidator,
            eventProducer = eventProducer
        )
    }

    private fun createTestUser(
        id: UUID = testUserId,
        email: String = testEmail,
        username: String = testUsername,
        isEmailVerified: Boolean = true,
        isActive: Boolean = true,
        passwordHash: String = testPasswordHash
    ): User = User(
        id = id,
        username = username,
        email = email,
        passwordHash = passwordHash,
        firstName = testFirstName,
        lastName = testLastName,
        middleName = testMiddleName,
        avatarUrl = null,
        role = UserRole.ADMIN,
        isActive = isActive,
        isEmailVerified = isEmailVerified,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now(),
        lastLoginAt = Clock.System.now()
    )

    // ================================ REGISTER =================================

    @Test
    fun `register - success`() = runBlocking {
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = testPassword,
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        coEvery { userRepository.existsByEmail(testEmail) } returns false
        coEvery { userRepository.existsByUsername(testUsername) } returns false
        coEvery { passwordEncoder.encode(testPassword) } returns testPasswordHash
        coEvery { userRepository.upsert(any()) } answers { firstArg() }
        coEvery { jwtProvider.generateAccessToken(any()) } returns testAccessToken
        coEvery { jwtProvider.generateRefreshToken(any()) } returns testRefreshToken

        val result = authService.register(request, "127.0.0.1")

        assertEquals(testAccessToken, result.token)
        assertEquals(testRefreshToken, result.refreshToken)
    }

    @Test
    fun `register - fails when email already exists`() = runBlocking {
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = testPassword,
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        coEvery { userRepository.existsByEmail(testEmail) } returns true

        val exception = assertFailsWith<DomainException.AlreadyExists> {
            authService.register(request, "127.0.0.1")
        }
        assertEquals("Email '$testEmail' already exists", exception.message)
    }

    @Test
    fun `register - fails when username already exists`() = runBlocking {
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = testPassword,
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        coEvery { userRepository.existsByUsername(testUsername) } returns true

        val exception = assertFailsWith<DomainException.AlreadyExists> {
            authService.register(request, "127.0.0.1")
        }
        assertEquals("Username '$testUsername' already exists", exception.message)
    }

    @Test
    fun `register - fails with invalid email format`() = runBlocking {
        val request = UserCreate(
            email = "invalid-email",
            username = testUsername,
            password = testPassword,
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.register(request, "127.0.0.1")
        }
        assertEquals("Invalid email format", exception.message)
    }

    @Test
    fun `register - fails with too short username`() = runBlocking {
        val request = UserCreate(
            email = testEmail,
            username = "ab",
            password = testPassword,
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.register(request, "127.0.0.1")
        }
        assertEquals("Username must be between 3 and 50 characters", exception.message)
    }

    @Test
    fun `register - fails with invalid characters in username`() = runBlocking {
        val request = UserCreate(
            email = testEmail,
            username = "user@name",
            password = testPassword,
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.register(request, "127.0.0.1")
        }
        assertEquals("Username contains invalid characters", exception.message)
    }

    @Test
    fun `register - fails with short password`() = runBlocking {
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = "short",
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.register(request, "127.0.0.1")
        }
        assertEquals("Password must be at least 8 characters", exception.message)
    }

    @Test
    fun `register - fails with password without digit`() = runBlocking {
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = "passwordonly",
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.register(request, "127.0.0.1")
        }
        assertEquals("Password must contain at least one digit", exception.message)
    }

    @Test
    fun `register - fails with password without letter`() = runBlocking {
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = "12345678",
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.register(request, "127.0.0.1")
        }
        assertEquals("Password must contain at least one letter", exception.message)
    }

    // ================================ LOGIN =================================

    @Test
    fun `login - success with email`() = runBlocking {
        val request = UserLogin(name = testEmail, password = testPassword)
        val user = createTestUser()

        coEvery { userRepository.findByEmail(testEmail) } returns user
        coEvery { passwordEncoder.verify(testPassword, testPasswordHash) } returns true
        coEvery { jwtProvider.generateAccessToken(user) } returns testAccessToken
        coEvery { jwtProvider.generateRefreshToken(user) } returns testRefreshToken
        coEvery { userRepository.upsert(any()) } returns user

        val result = authService.login(request, "Auth Test")

        assertEquals(testAccessToken, result.token)
        assertEquals(testRefreshToken, result.refreshToken)

        coVerify { userRepository.upsert(match { it.lastLoginAt != null }) }
    }

    @Test
    fun `login - success with username`() = runBlocking {
        val request = UserLogin(name = testUsername, password = testPassword)
        val user = createTestUser()

        coEvery { userRepository.findByUsername(testUsername) } returns user
        coEvery { passwordEncoder.verify(testPassword, testPasswordHash) } returns true
        coEvery { jwtProvider.generateAccessToken(user) } returns testAccessToken
        coEvery { jwtProvider.generateRefreshToken(user) } returns testRefreshToken
        coEvery { userRepository.upsert(any()) } returns user

        val result = authService.login(request, "Auth Test")

        assertEquals(testAccessToken, result.token)
        assertEquals(testRefreshToken, result.refreshToken)
    }

    @Test
    fun `login - fails when user not found`() = runBlocking {
        val request = UserLogin(name = "nonexistent@example.com", password = testPassword)

        coEvery { userRepository.findByEmail("nonexistent@example.com") } returns null

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.login(request, "Auth Test")
        }
        assertEquals("Invalid login or password", exception.message)
    }

    @Test
    fun `login - fails when password incorrect`() = runBlocking {
        val request = UserLogin(name = testEmail, password = "wrong")
        val user = createTestUser()

        coEvery { userRepository.findByEmail(testEmail) } returns user
        coEvery { passwordEncoder.verify("wrong", testPasswordHash) } returns false

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.login(request, "Auth Test")
        }
        assertEquals("Invalid login or password", exception.message)
    }

    @Test
    fun `login - fails when user is inactive`() = runBlocking {
        val request = UserLogin(name = testEmail, password = testPassword)
        val user = createTestUser(isActive = false)

        coEvery { userRepository.findByEmail(testEmail) } returns user
        coEvery { passwordEncoder.verify(testPassword, testPasswordHash) } returns true

        val exception = assertFailsWith<DomainException.BusinessRule> {
            authService.login(request, "Auth Test")
        }
        assertEquals("User is deactivated", exception.message)
    }

    // ================================ REFRESH TOKEN =================================

    @Test
    fun `refreshToken - success`() = runBlocking {
        val refreshToken = "valid.refresh.token"
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            role = "USER",
            error = null,
            isExpired = false
        )
        val user = createTestUser(isActive = true)

        coEvery { jwtValidator.validateToken(refreshToken) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns user
        coEvery { jwtProvider.generateAccessToken(user) } returns "newAccessToken"
        coEvery { jwtProvider.generateRefreshToken(user) } returns "newRefreshToken"

        val result = authService.refreshToken(refreshToken)

        assertEquals("newAccessToken", result.token)
        assertEquals("newRefreshToken", result.refreshToken)
        coVerify { userRepository.findById(testUserId) }
    }

    @Test
    fun `refreshToken - fails with invalid token`() = runBlocking {
        val invalidToken = "invalid.token"
        val validationResult = TokenValidationResult(
            isValid = false,
            userId = null,
            role = null,
            error = "Invalid token",
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(invalidToken) } returns validationResult

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.refreshToken(invalidToken)
        }
        assertEquals("Invalid refresh token", exception.message)
        coVerify(exactly = 0) { userRepository.findById(any()) }
    }

    @Test
    fun `refreshToken - fails with expired token`() = runBlocking {
        val expiredToken = "expired.token"
        val validationResult = TokenValidationResult(
            isValid = false,
            userId = null,
            role = null,
            error = "Token expired",
            isExpired = true
        )
        coEvery { jwtValidator.validateToken(expiredToken) } returns validationResult

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.refreshToken(expiredToken)
        }
        assertEquals("Invalid refresh token", exception.message)
    }

    @Test
    fun `refreshToken - fails when user not found`() = runBlocking {
        val refreshToken = "valid.token"
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            role = "USER",
            error = null,
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(refreshToken) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns null

        val exception = assertFailsWith<DomainException.NotFound> {
            authService.refreshToken(refreshToken)
        }
        assertEquals("User with '$testUserId' not found", exception.message)
    }

    @Test
    fun `refreshToken - fails when user deactivated`() = runBlocking {
        val refreshToken = "valid.token"
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            role = "USER",
            error = null,
            isExpired = false
        )
        val user = createTestUser(isActive = false)

        coEvery { jwtValidator.validateToken(refreshToken) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns user

        val exception = assertFailsWith<DomainException.BusinessRule> {
            authService.refreshToken(refreshToken)
        }
        assertEquals("User is deactivated", exception.message)
        coVerify(exactly = 0) { jwtProvider.generateAccessToken(any()) }
    }

    // ================================ VALIDATE TOKEN =================================

    @Test
    fun `validateToken - success`() = runBlocking {
        val token = "valid.access.token"
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            role = "ADMIN",
            error = null,
            isExpired = false
        )
        val user = createTestUser()

        coEvery { jwtValidator.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns user

        val userResponse = authService.validateToken(token)

        assertEquals(testEmail, userResponse.email)
        assertEquals(testUsername, userResponse.username)
        assertEquals(UserRole.ADMIN.name, userResponse.role)
    }

    @Test
    fun `validateToken - fails with invalid token`() = runBlocking {
        val invalidToken = "invalid.token"
        val validationResult = TokenValidationResult(
            isValid = false,
            userId = null,
            role = null,
            error = "Malformed token",
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(invalidToken) } returns validationResult

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.validateToken(invalidToken)
        }
        assertEquals("Malformed token", exception.message)
    }

    @Test
    fun `validateToken - fails when user not found`() = runBlocking {
        val token = "valid.token"
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            role = "USER",
            error = null,
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns null

        val exception = assertFailsWith<DomainException.NotFound> {
            authService.validateToken(token)
        }
        assertEquals("User with '$testUserId' not found", exception.message)
    }

    @Test
    fun `validateToken - fails when user deactivated`() = runBlocking {
        val token = "valid.token"
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            role = "USER",
            error = null,
            isExpired = false
        )
        val user = createTestUser(isActive = false)

        coEvery { jwtValidator.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns user

        val exception = assertFailsWith<DomainException.BusinessRule> {
            authService.validateToken(token)
        }
        assertEquals("User is deactivated", exception.message)
    }

    // ================================ CHANGE PASSWORD (with current password) =================================

    @Test
    fun `changePassword - success`() = runBlocking {
        val userId = testUserId
        val currentPassword = "oldPass123"
        val newPassword = "newPass456"
        val oldHash = passwordEncoder.encode(currentPassword)
        val newHash = passwordEncoder.encode(newPassword)

        val user = createTestUser(id = userId, isActive = true, passwordHash = oldHash)

        coEvery { userRepository.findById(userId) } returns user
        coEvery { passwordEncoder.verify(currentPassword, oldHash) } returns true
        coEvery { passwordEncoder.encode(newPassword) } returns newHash
        coEvery { userRepository.upsert(any()) } returns user

        authService.changePassword(userId, currentPassword, newPassword)

        coVerify {
            userRepository.findById(userId)
            passwordEncoder.verify(currentPassword, oldHash)
            passwordEncoder.encode(newPassword)
            userRepository.upsert(match { it.passwordHash == newHash && it.id == userId })
        }
    }

    @Test
    fun `changePassword - fails when user inactive`() = runBlocking {
        val userId = testUserId
        val user = createTestUser(id = userId, isActive = false)

        coEvery { userRepository.findById(userId) } returns user

        val exception = assertFailsWith<DomainException.BusinessRule> {
            authService.changePassword(userId, "old", "new")
        }
        assertEquals("User is deactivated", exception.message)
        coVerify(exactly = 0) { passwordEncoder.verify(any(), any()) }
    }

    @Test
    fun `changePassword - fails when current password incorrect`() = runBlocking {
        val userId = testUserId
        val currentPassword = "wrong"
        val newPassword = "newPass456"
        val oldHash = passwordEncoder.encode("correctPass")
        val user = createTestUser(id = userId, isActive = true, passwordHash = oldHash)

        coEvery { userRepository.findById(userId) } returns user
        coEvery { passwordEncoder.verify(currentPassword, oldHash) } returns false

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.changePassword(userId, currentPassword, newPassword)
        }
        assertEquals("Current password is incorrect", exception.message)
        coVerify(exactly = 0) { userRepository.upsert(any()) }
    }

    @Test
    fun `changePassword - fails when new password invalid`() = runBlocking {
        val userId = testUserId
        val currentPassword = "oldPass123"
        val newPassword = "short"
        val user = createTestUser(id = userId, isActive = true)

        coEvery { userRepository.findById(userId) } returns user
        coEvery { passwordEncoder.verify(currentPassword, user.passwordHash) } returns true

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.changePassword(userId, currentPassword, newPassword)
        }
        assertEquals("Password must be at least 8 characters", exception.message)
        coVerify(exactly = 0) { userRepository.upsert(any()) }
    }

    // ================================ CHANGE PASSWORD (admin forced) =================================

    @Test
    fun `changePassword - admin forced success`() = runBlocking {
        val userId = testUserId
        val newPassword = "newSecurePass789"
        val newHash = passwordEncoder.encode(newPassword)
        val user = createTestUser(id = userId, isActive = true, passwordHash = "oldHash")

        coEvery { userRepository.findById(userId) } returns user
        coEvery { passwordEncoder.encode(newPassword) } returns newHash
        coEvery { userRepository.upsert(any()) } returns user

        authService.changePassword(userId, newPassword)

        coVerify {
            userRepository.findById(userId)
            passwordEncoder.encode(newPassword)
            userRepository.upsert(match {
                it.passwordHash == newHash &&
                        it.id == userId &&
                        !it.isNeedChangePassword
            })
        }
    }

    @Test
    fun `changePassword - admin forced fails when new password same as current`() = runBlocking {
        val userId = testUserId
        val samePassword = testPassword
        val user = createTestUser(id = userId, isActive = true, passwordHash = testPasswordHash)

        coEvery { userRepository.findById(userId) } returns user
        coEvery { passwordEncoder.verify(samePassword, testPasswordHash) } returns true

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.changePassword(userId, samePassword)
        }
        assertEquals("The new password cannot be equal to the current one.t", exception.message)
        coVerify(exactly = 0) { userRepository.upsert(any()) }
    }

    @Test
    fun `changePassword - admin forced fails when new password invalid`() = runBlocking {
        val userId = testUserId
        val newPassword = "weak"
        val user = createTestUser(id = userId, isActive = true)

        coEvery { userRepository.findById(userId) } returns user
        coEvery { passwordEncoder.verify(newPassword, user.passwordHash) } returns false

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.changePassword(userId, newPassword)
        }
        assertEquals("Password must be at least 8 characters", exception.message)
        coVerify(exactly = 0) { userRepository.upsert(any()) }
    }

    // ================================ FORGOT PASSWORD =================================

    @Test
    fun `forgotPassword - does nothing when user not found (security by obscurity)`() = runBlocking {
        coEvery { userRepository.findByEmail(testEmail) } returns null

        // Should not throw any exception
        authService.forgotPassword(testEmail)

        coVerify(exactly = 1) { userRepository.findByEmail(testEmail) }
        coVerify(exactly = 0) { userRepository.upsert(any()) }
    }

    @Test
    fun `forgotPassword - does not throw when user exists and active (but no email send mock)`() = runBlocking {
        val user = createTestUser(isActive = true)
        coEvery { userRepository.findByEmail(testEmail) } returns user

        authService.forgotPassword(testEmail)

        coVerify { userRepository.findByEmail(testEmail) }
    }

    @Test
    fun `forgotPassword - logs warning when user exists but inactive`() = runBlocking {
        val user = createTestUser(isActive = false)
        coEvery { userRepository.findByEmail(testEmail) } returns user

        authService.forgotPassword(testEmail)

        coVerify { userRepository.findByEmail(testEmail) }
        // No further actions
    }

    // ================================ RESET PASSWORD =================================

    @Test
    fun `resetPassword - success`() = runBlocking {
        val token = "reset.token"
        val newPassword = "NewPass789"
        val newHash = passwordEncoder.encode(newPassword)
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            role = "USER",
            error = null,
            isExpired = false
        )
        val user = createTestUser(isActive = true)

        coEvery { jwtValidator.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns user
        coEvery { passwordEncoder.encode(newPassword) } returns newHash
        coEvery { userRepository.upsert(any()) } returns user

        authService.resetPassword(token, newPassword)

        coVerify {
            jwtValidator.validateToken(token)
            userRepository.findById(testUserId)
            passwordEncoder.encode(newPassword)
            userRepository.upsert(match { it.passwordHash == newHash && it.id == testUserId })
        }
    }

    @Test
    fun `resetPassword - fails with invalid token`() = runBlocking {
        val token = "invalid.token"
        val validationResult = TokenValidationResult(
            isValid = false,
            userId = null,
            role = null,
            error = "Invalid token",
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(token) } returns validationResult

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.resetPassword(token, "newPass")
        }
        assertEquals("Invalid or expired token", exception.message)
        coVerify(exactly = 0) { userRepository.findById(any()) }
    }

    @Test
    fun `resetPassword - fails when user not found`() = runBlocking {
        val token = "valid.token"
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            role = "USER",
            error = null,
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns null

        val exception = assertFailsWith<DomainException.NotFound> {
            authService.resetPassword(token, "newPass")
        }
        assertEquals("User with '$testUserId' not found", exception.message)
    }

    @Test
    fun `resetPassword - fails when new password invalid`() = runBlocking {
        val token = "reset.token"
        val newPassword = "short"
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            role = "USER",
            error = null,
            isExpired = false
        )
        val user = createTestUser(isActive = true)

        coEvery { jwtValidator.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns user

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.resetPassword(token, newPassword)
        }
        assertEquals("Password must be at least 8 characters", exception.message)
        coVerify(exactly = 0) { userRepository.upsert(any()) }
    }

    // ================================ VERIFY EMAIL =================================

    @Test
    fun `verifyEmail - success`() = runBlocking {
        val token = "verify.token"
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            role = "USER",
            error = null,
            isExpired = false
        )
        val user = createTestUser(isEmailVerified = false)

        coEvery { jwtValidator.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns user
        coEvery { userRepository.upsert(any()) } returns user

        authService.verifyEmail(token)

        coVerify {
            jwtValidator.validateToken(token)
            userRepository.findById(testUserId)
            userRepository.upsert(match { it.isEmailVerified && it.id == testUserId })
        }
    }

    @Test
    fun `verifyEmail - fails with invalid token`() = runBlocking {
        val token = "invalid.token"
        val validationResult = TokenValidationResult(
            isValid = false,
            userId = null,
            role = null,
            error = "Malformed token",
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(token) } returns validationResult

        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.verifyEmail(token)
        }
        assertEquals("Invalid or expired token", exception.message)
        coVerify(exactly = 0) { userRepository.findById(any()) }
    }

    @Test
    fun `verifyEmail - fails when user not found`() = runBlocking {
        val token = "valid.token"
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            role = "USER",
            error = null,
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns null

        val exception = assertFailsWith<DomainException.NotFound> {
            authService.verifyEmail(token)
        }
        assertEquals("User with '$testUserId' not found", exception.message)
    }
}