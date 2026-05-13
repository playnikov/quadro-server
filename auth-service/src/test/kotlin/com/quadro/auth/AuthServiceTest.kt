package com.quadro.auth

import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserLogin
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.domain.services.AuthService
import com.quadro.auth.domain.services.AuthServiceImpl
import com.quadro.auth.domain.services.UserService
import com.quadro.auth.domain.services.UserServiceImpl
import com.quadro.auth.infrastructure.security.JwtProvider
import com.quadro.auth.infrastructure.security.PasswordEncoder
import com.quadro.auth.presentation.models.LoginRequest
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.JwtValidator
import com.quadro.shared.security.TokenValidationResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock

class AuthServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var jwtValidator: JwtValidator
    private lateinit var jwtProvider: JwtProvider
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authService: AuthService
    private lateinit var eventProducer: EventProducer
    private lateinit var userService: UserService

    private val testUserId = UUID.randomUUID()
    private val testEmail = "test@example.com"
    private val testUsername = "testuser"
    private val testPassword = "password123"
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
        testPasswordHash = passwordEncoder.encode(testPassword)
        eventProducer = mockk(relaxed = true)
        authService = AuthServiceImpl(
            userRepository = userRepository,
            passwordEncoder = passwordEncoder,
            jwtProvider = jwtProvider,
            jwtValidator = jwtValidator,
            eventProducer = eventProducer
        )
        userService = UserServiceImpl(
            userRepository = userRepository
        )
    }

    private fun createTestUser(
        id: UUID = testUserId,
        email: String = testEmail,
        username: String = testUsername,
        isEmailVerified: Boolean = true,
        isActive: Boolean = true
    ): User = User(
        id = id,
        username = username,
        email = email,
        passwordHash = testPasswordHash,
        firstName = testFirstName,
        lastName = testLastName,
        middleName = testMiddleName,
        avatarUrl = null,
        role = UserRole.ADMIN,
        isActive = isActive,
        isEmailVerified = isEmailVerified,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now(),
        lastLoginAt = Clock.System.now(),
        lastLoginIp = "localhost"
    )

    @Test
    fun `register - успешная регистрация`() = runBlocking {
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
        coEvery { userRepository.create(any()) } answers { firstArg() }
        coEvery { jwtProvider.generateAccessToken(any()) } returns testAccessToken
        coEvery { jwtProvider.generateRefreshToken(any()) } returns testRefreshToken

        val result = authService.register(request, "localhost")

        assertEquals(testAccessToken, result.token)
        assertEquals(testRefreshToken, result.refreshToken)
    }

    @Test
    fun `register - ошибка регистрации, почта уже занята`() = runBlocking {
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = testPassword,
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        coEvery { userRepository.existsByEmail(testEmail) } returns true

        val exception = assertFails {
            authService.register(request, "localhost")
        }
        assertTrue(exception is DomainException.AlreadyExists)
        assertEquals("Email '$testEmail' already exists", exception.message)
    }

    @Test
    fun `register - ошибка регистрации, имя пользоватя уже занято`() = runBlocking {
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = testPassword,
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        coEvery { userRepository.existsByUsername(testUsername) } returns true

        val exception = assertFails {
            authService.register(request, "localhost")
        }
        assertTrue(exception is DomainException.AlreadyExists)
        assertEquals("Username '$testUsername' already exists", exception.message)
    }

    @Test
    fun `register - неправильный формат почты`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = "invalid-email",
            username = testUsername,
            password = testPassword,
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFails {
            authService.register(request, "localhost")
        }
        assertTrue(exception is DomainException.ValidationError)
        assertEquals("Invalid email format", exception.message)
    }

    @Test
    fun `register - неправильная длина имя пользователя`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = "ab",
            password = testPassword,
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFails {
            authService.register(request, "localhost")
        }
        assertTrue(exception is DomainException.ValidationError)
        assertEquals("Username must be between 3 and 50 characters", exception.message)
    }

    @Test
    fun `register - неправильный формат имя пользователя`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = "user@name",
            password = testPassword,
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFails {
            authService.register(request, "localhost")
        }
        assertTrue(exception is DomainException.ValidationError)
        assertEquals("Username contains invalid characters", exception.message)
    }


    @Test
    fun `register - короткий пароль`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = "short",
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFails {
            authService.register(request, "localhost")
        }
        assertTrue(exception is DomainException.ValidationError)
        assertEquals("Password must be at least 8 characters", exception.message)
    }

    @Test
    fun `register - нет цифр в пароле`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = "passwordonly",
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFails {
            authService.register(request, "localhost")
        }
        assertTrue(exception is DomainException.ValidationError)
        assertEquals("Password must contain at least one digit", exception.message)
    }

    @Test
    fun `register - нет букв в пароле`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = "12345678",
            firstName = testFirstName,
            lastName = testLastName,
            middleName = testMiddleName,
        )

        val exception = assertFails {
            authService.register(request, "localhost")
        }
        assertTrue(exception is DomainException.ValidationError)
        assertEquals("Password must contain at least one letter", exception.message)
    }

    @Test
    fun `login - успешная авторизация по почте`() = runBlocking {
        // Arrange
        val request = UserLogin(
            name = testEmail,
            password = testPassword
        )

        val user = createTestUser()

        coEvery { userRepository.findByEmail(testEmail) } returns user
        coEvery { passwordEncoder.verify(testPassword, testPasswordHash) } returns true
        coEvery { jwtProvider.generateAccessToken(user) } returns testAccessToken
        coEvery { jwtProvider.generateRefreshToken(user) } returns testRefreshToken

        // Act
        val result = authService.login(request, "localhost", "test")

        // Assert
        assertEquals(testAccessToken, result.token)
        assertEquals(testRefreshToken, result.refreshToken)
    }

    @Test
    fun `login - успешная авторизация по username`() = runBlocking {
        // Arrange
        val request = UserLogin(
            name = testUsername,
            password = testPassword
        )

        val user = createTestUser()

        coEvery { userRepository.findByUsername(testUsername) } returns user
        coEvery { passwordEncoder.verify(testPassword, testPasswordHash) } returns true
        coEvery { jwtProvider.generateAccessToken(user) } returns testAccessToken
        coEvery { jwtProvider.generateRefreshToken(user) } returns testRefreshToken

        // Act
        val result = authService.login(request, "localhost", "test")

        // Assert
        assertEquals(testAccessToken, result.token)
        assertEquals(testRefreshToken, result.refreshToken)
    }

    @Test
    fun `login - ошибка входа, пользователь не найден`() = runBlocking {
        // Arrange
        val request = UserLogin(
            name = "nonexistent@example.com",
            password = testPassword
        )

        // Act & Assert
        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.login(request, "localhost", "test")
        }
        assertEquals("Invalid login or password", exception.message)
    }

    @Test
    fun `login - ошибка входа, неверный пароль`() = runBlocking {
        // Arrange
        val request = UserLogin(
            name = testEmail,
            password = "wrongpassword"
        )

        val user = createTestUser()

        coEvery { userRepository.findByEmail(testEmail) } returns user

        // Act & Assert
        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.login(request, "localhost", "test")
        }
        assertEquals("Invalid login or password", exception.message)
    }

    @Test
    fun `login - ошибка входа, неактивный пользователь`() = runBlocking {
        // Arrange
        val request = UserLogin(
            name = testEmail,
            password = testPassword
        )

        val user = createTestUser(isActive = false)

        coEvery { passwordEncoder.verify(testPassword, testPasswordHash) } returns true
        coEvery { userRepository.findByEmail(testEmail) } returns user

        // Act & Assert
        val exception = assertFailsWith<DomainException.BusinessRule> {
            authService.login(request, "localhost", "test")
        }
        assertEquals("User is deactivated", exception.message)
    }

    @Test
    fun `refreshToken - успешное обновление токенов`() = runBlocking {
        // Arrange
        val refreshToken = "valid.refresh.token"
        val userId = testUserId
        val role = "USER"

        val validationResult = TokenValidationResult(
            isValid = true,
            userId = userId,
            role = role,
            error = null,
            isExpired = false
        )
        val user = createTestUser(id = userId, isActive = true)

        coEvery { jwtValidator.validateToken(refreshToken) } returns validationResult
        coEvery { userRepository.findById(userId) } returns user
        coEvery { jwtProvider.generateAccessToken(user) } returns "newAccessToken"
        coEvery { jwtProvider.generateRefreshToken(user) } returns "newRefreshToken"

        // Act
        val result = authService.refreshToken(refreshToken)

        // Assert
        assertEquals("newAccessToken", result.token)
        assertEquals("newRefreshToken", result.refreshToken)
        coVerify { userRepository.findById(userId) }
        coVerify { jwtProvider.generateAccessToken(user) }
        coVerify { jwtProvider.generateRefreshToken(user) }
    }

    @Test
    fun `refreshToken - невалидный refresh token`() = runBlocking {
        // Arrange
        val invalidToken = "invalid.token"
        val validationResult = TokenValidationResult(
            isValid = false,
            userId = null,
            role = null,
            error = "Invalid token",
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(invalidToken) } returns validationResult

        // Act & Assert
        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.refreshToken(invalidToken)
        }
        assertEquals("Invalid refresh token", exception.message)
        coVerify(exactly = 0) { userRepository.findById(any()) }
    }

    @Test
    fun `refreshToken - токен просрочен`() = runBlocking {
        // Arrange
        val expiredToken = "expired.token"
        val validationResult = TokenValidationResult(
            isValid = false,
            userId = null,
            role = null,
            error = "Token expired",
            isExpired = true
        )
        coEvery { jwtValidator.validateToken(expiredToken) } returns validationResult

        // Act & Assert
        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.refreshToken(expiredToken)
        }
        assertEquals("Invalid refresh token", exception.message)
    }

    @Test
    fun `refreshToken - валидный токен, но пользователь не найден`() = runBlocking {
        // Arrange
        val refreshToken = "valid.token"
        val userId = UUID.randomUUID()
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = userId,
            role = "USER",
            error = null,
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(refreshToken) } returns validationResult
        coEvery { userRepository.findById(userId) } returns null

        // Act & Assert
        val exception = assertFailsWith<DomainException.NotFound> {
            authService.refreshToken(refreshToken)
        }
        assertEquals("User with id '$userId' not found", exception.message)
    }

    @Test
    fun `refreshToken - пользователь деактивирован`() = runBlocking {
        // Arrange
        val refreshToken = "valid.token"
        val userId = testUserId
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = userId,
            role = "USER",
            error = null,
            isExpired = false
        )
        val user = createTestUser(id = userId, isActive = false)

        coEvery { jwtValidator.validateToken(refreshToken) } returns validationResult
        coEvery { userRepository.findById(userId) } returns user

        // Act & Assert
        val exception = assertFailsWith<DomainException.BusinessRule> {
            authService.refreshToken(refreshToken)
        }
        assertEquals("User is deactivated", exception.message)
        coVerify(exactly = 0) { jwtProvider.generateAccessToken(any()) }
    }

    @Test
    fun `validateToken - успешная валидация токена`() = runBlocking {
        // Arrange
        val token = "valid.access.token"
        val userId = testUserId
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = userId,
            role = "ADMIN",
            error = null,
            isExpired = false
        )
        val user = createTestUser(id = userId, isActive = true)

        coEvery { jwtValidator.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(userId) } returns user

        // Act
        val userResponse = authService.validateToken(token)

        // Assert
        assertEquals(testEmail, userResponse.email)
        assertEquals(testUsername, userResponse.username)
        assertEquals(UserRole.ADMIN.name, userResponse.role)
    }

    @Test
    fun `validateToken - невалидный токен`() = runBlocking {
        // Arrange
        val invalidToken = "invalid.token"
        val validationResult = TokenValidationResult(
            isValid = false,
            userId = null,
            role = null,
            error = "Malformed token",
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(invalidToken) } returns validationResult

        // Act & Assert
        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.validateToken(invalidToken)
        }
        assertEquals("Malformed token", exception.message)
    }

    @Test
    fun `validateToken - токен просрочен`() = runBlocking {
        // Arrange
        val expiredToken = "expired.token"
        val validationResult = TokenValidationResult(
            isValid = false,
            userId = null,
            role = null,
            error = "Token expired",
            isExpired = true
        )
        coEvery { jwtValidator.validateToken(expiredToken) } returns validationResult

        // Act & Assert
        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.validateToken(expiredToken)
        }
        assertEquals("Token expired", exception.message)
    }

    @Test
    fun `validateToken - валидный токен, но пользователь не найден`() = runBlocking {
        // Arrange
        val token = "valid.token"
        val userId = UUID.randomUUID()
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = userId,
            role = "USER",
            error = null,
            isExpired = false
        )
        coEvery { jwtValidator.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(userId) } returns null

        // Act & Assert
        val exception = assertFailsWith<DomainException.NotFound> {
            authService.validateToken(token)
        }
        assertEquals("User with id '$userId' not found", exception.message)
    }

    @Test
    fun `validateToken - пользователь деактивирован`() = runBlocking {
        // Arrange
        val token = "valid.token"
        val userId = testUserId
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = userId,
            role = "USER",
            error = null,
            isExpired = false
        )
        val user = createTestUser(id = userId, isActive = false)

        coEvery { jwtValidator.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(userId) } returns user

        // Act & Assert
        val exception = assertFailsWith<DomainException.BusinessRule> {
            authService.validateToken(token)
        }
        assertEquals("User is deactivated", exception.message)
    }

    @Test
    fun `changePassword - успешная смена пароля`() = runBlocking {
        // Arrange
        val userId = testUserId
        val currentPassword = "oldPass123"
        val newPassword = "newPass456"
        val oldHash = passwordEncoder.encode(currentPassword)
        val newHash = passwordEncoder.encode(newPassword)

        val user = createTestUser(id = userId, isActive = true).copy(passwordHash = oldHash)

        coEvery { userRepository.findById(userId) } returns user
        coEvery { passwordEncoder.verify(currentPassword, oldHash) } returns true
        coEvery { passwordEncoder.encode(newPassword) } returns newHash

        // Act
        authService.changePassword(userId, currentPassword, newPassword)

        // Assert
        coVerify {
            userRepository.findById(userId)
            passwordEncoder.verify(currentPassword, oldHash)
            passwordEncoder.encode(newPassword)
            userRepository.update(match { it.passwordHash == newHash && it.id == userId })
        }
    }

    @Test
    fun `changePassword - пользователь не найден`() = runBlocking {
        // Arrange
        val userId = UUID.randomUUID()
        coEvery { userRepository.findById(userId) } returns null

        // Act & Assert
        val exception = assertFailsWith<DomainException.NotFound> {
            authService.changePassword(userId, "any", "any")
        }
        assertEquals("User with id '$userId' not found", exception.message)
    }

    @Test
    fun `changePassword - пользователь деактивирован`() = runBlocking {
        // Arrange
        val userId = testUserId
        val user = createTestUser(id = userId, isActive = false)

        coEvery { userRepository.findById(userId) } returns user

        // Act & Assert
        val exception = assertFailsWith<DomainException.BusinessRule> {
            authService.changePassword(userId, "old", "new")
        }
        assertEquals("User is deactivated", exception.message)
        coVerify(exactly = 0) { passwordEncoder.verify(any(), any()) }
    }

    @Test
    fun `changePassword - неверный текущий пароль`() = runBlocking {
        // Arrange
        val userId = testUserId
        val currentPassword = "wrongPassword"
        val newPassword = "newPass456"
        val oldHash = passwordEncoder.encode("correctPass")

        val user = createTestUser(id = userId, isActive = true).copy(passwordHash = oldHash)

        coEvery { userRepository.findById(userId) } returns user
        coEvery { passwordEncoder.verify(currentPassword, oldHash) } returns false

        // Act & Assert
        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.changePassword(userId, currentPassword, newPassword)
        }
        assertEquals("Current password is incorrect", exception.message)
        coVerify(exactly = 0) { userRepository.update(any()) }
    }

    @Test
    fun `changePassword - новый пароль не соответствует требованиям`() = runBlocking {
        // Arrange
        val userId = testUserId
        val currentPassword = "oldPass123"
        val newPassword = "short" // слишком короткий

        val user = createTestUser(id = userId, isActive = true)
        coEvery { userRepository.findById(userId) } returns user
        coEvery { passwordEncoder.verify(currentPassword, user.passwordHash) } returns true

        // Act & Assert
        val exception = assertFailsWith<DomainException.ValidationError> {
            authService.changePassword(userId, currentPassword, newPassword)
        }
        assertEquals("Password must be at least 8 characters", exception.message)
        coVerify(exactly = 0) { userRepository.update(any()) }
    }
}