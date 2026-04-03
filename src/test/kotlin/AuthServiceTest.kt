package com.quadro

import com.quadro.datasource.repositories.users.UserRepository
import com.quadro.domain.models.user.DomainUserRole
import com.quadro.domain.models.user.TokenValidationResult
import com.quadro.domain.models.user.User
import com.quadro.domain.models.user.UserCreate
import com.quadro.domain.models.user.UserLogin
import com.quadro.domain.services.auth.AuthService
import com.quadro.domain.services.auth.AuthServiceImpl
import com.quadro.security.JwtTokenService
import com.quadro.security.PasswordEncoder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var jwtTokenService: JwtTokenService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authService: AuthServiceImpl

    private val testUserId = UUID.randomUUID()
    private val testEmail = "test@example.com"
    private val testUsername = "testuser"
    private val testPassword = "password123"
    private val testPasswordHash = "hashed_password_123"
    private val testAccessToken = "access.token.string"
    private val testRefreshToken = "refresh.token.string"

    @Before
    fun setup() {
        userRepository = mockk(relaxed = true)
        jwtTokenService = mockk(relaxed = true)
        passwordEncoder = mockk(relaxed = true)
        authService = AuthServiceImpl(
            userRepository = userRepository,
            jwtTokenService = jwtTokenService,
            passwordEncoder = passwordEncoder
        )
    }

    @Test
    fun `register - should create user successfully`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = testPassword,
            firstName = "Test",
            lastName = "User",
            role = DomainUserRole.USER
        )

        coEvery { userRepository.existsByEmail(testEmail) } returns false
        coEvery { userRepository.existsByUsername(testUsername) } returns false
        coEvery { passwordEncoder.encode(testPassword) } returns testPasswordHash
        coEvery { userRepository.create(any()) } answers { firstArg() }
        coEvery { jwtTokenService.generateAccessToken(any()) } returns testAccessToken
        coEvery { jwtTokenService.generateRefreshToken(any()) } returns testRefreshToken

        // Act
        val result = authService.register(request)

        // Assert
        assertTrue(result.isSuccess)
        val authResult = result.getOrNull()
        assertEquals(testAccessToken, authResult?.token)
        assertEquals(testRefreshToken, authResult?.refreshToken)

        coVerify(exactly = 1) { userRepository.create(any()) }
        coVerify(exactly = 1) { jwtTokenService.generateAccessToken(any()) }
        coVerify(exactly = 1) { jwtTokenService.generateRefreshToken(any()) }
    }

    @Test
    fun `register - should fail when email already exists`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = testPassword,
            firstName = null,
            lastName = null,
            role = DomainUserRole.USER
        )

        coEvery { userRepository.existsByEmail(testEmail) } returns true

        // Act
        val result = authService.register(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Email already registered", result.exceptionOrNull()?.message)

        coVerify(exactly = 0) { userRepository.create(any()) }
    }

    @Test
    fun `register - should fail when username already taken`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = testPassword,
            firstName = null,
            lastName = null,
            role = DomainUserRole.USER
        )

        coEvery { userRepository.existsByEmail(testEmail) } returns false
        coEvery { userRepository.existsByUsername(testUsername) } returns true

        // Act
        val result = authService.register(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Username already taken", result.exceptionOrNull()?.message)

        coVerify(exactly = 0) { userRepository.create(any()) }
    }

    @Test
    fun `register - should validate email format`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = "invalid-email",
            username = testUsername,
            password = testPassword,
            firstName = null,
            lastName = null,
            role = DomainUserRole.USER
        )

        // Act
        val result = authService.register(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Invalid email format", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register - should validate username length`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = "ab", // too short
            password = testPassword,
            firstName = null,
            lastName = null,
            role = DomainUserRole.USER
        )

        // Act
        val result = authService.register(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Username must be between 3 and 50 characters", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register - should validate username characters`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = "user@name", // invalid character @
            password = testPassword,
            firstName = null,
            lastName = null,
            role = DomainUserRole.USER
        )

        // Act
        val result = authService.register(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Username contains invalid characters", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register - should validate password length`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = "short", // less than 8 chars
            firstName = null,
            lastName = null,
            role = DomainUserRole.USER
        )

        // Act
        val result = authService.register(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Password must be at least 8 characters", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register - should validate password contains digit`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = "passwordonly", // no digits
            firstName = null,
            lastName = null,
            role = DomainUserRole.USER
        )

        // Act
        val result = authService.register(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Password must contain at least one digit", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register - should validate password contains letter`() = runBlocking {
        // Arrange
        val request = UserCreate(
            email = testEmail,
            username = testUsername,
            password = "12345678", // no letters
            firstName = null,
            lastName = null,
            role = DomainUserRole.USER
        )

        // Act
        val result = authService.register(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Password must contain at least one letter", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login - should authenticate with email successfully`() = runBlocking {
        // Arrange
        val request = UserLogin(
            email = testEmail,
            username = null,
            password = testPassword
        )

        val user = createTestUser()

        coEvery { userRepository.findByEmail(testEmail) } returns user
        coEvery { passwordEncoder.verifyPassword(testPassword, testPasswordHash) } returns true
        coEvery { jwtTokenService.generateAccessToken(user) } returns testAccessToken
        coEvery { jwtTokenService.generateRefreshToken(user) } returns testRefreshToken

        // Act
        val result = authService.login(request)

        // Assert
        assertTrue(result.isSuccess)
        val authResult = result.getOrNull()
        assertEquals(testAccessToken, authResult?.token)
        assertEquals(testRefreshToken, authResult?.refreshToken)
    }

    @Test
    fun `login - should authenticate with username successfully`() = runBlocking {
        // Arrange
        val request = UserLogin(
            email = null,
            username = testUsername,
            password = testPassword
        )

        val user = createTestUser()

        coEvery { userRepository.findByUsername(testUsername) } returns user
        coEvery { passwordEncoder.verifyPassword(testPassword, testPasswordHash) } returns true
        coEvery { jwtTokenService.generateAccessToken(user) } returns testAccessToken
        coEvery { jwtTokenService.generateRefreshToken(user) } returns testRefreshToken

        // Act
        val result = authService.login(request)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `login - should fail with invalid email`() = runBlocking {
        // Arrange
        val request = UserLogin(
            email = "nonexistent@example.com",
            username = null,
            password = testPassword
        )

        coEvery { userRepository.findByEmail("nonexistent@example.com") } returns null

        // Act
        val result = authService.login(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login - should fail with invalid username`() = runBlocking {
        // Arrange
        val request = UserLogin(
            email = null,
            username = "nonexistent",
            password = testPassword
        )

        coEvery { userRepository.findByUsername("nonexistent") } returns null

        // Act
        val result = authService.login(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login - should fail with wrong password`() = runBlocking {
        // Arrange
        val request = UserLogin(
            email = testEmail,
            username = null,
            password = "wrongpassword"
        )

        val user = createTestUser()

        coEvery { userRepository.findByEmail(testEmail) } returns user
        coEvery { passwordEncoder.verifyPassword("wrongpassword", testPasswordHash) } returns false

        // Act
        val result = authService.login(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Invalid login or password", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login - should fail when user is deactivated`() = runBlocking {
        // Arrange
        val request = UserLogin(
            email = testEmail,
            username = null,
            password = testPassword
        )

        val user = createTestUser(isActive = false)

        coEvery { userRepository.findByEmail(testEmail) } returns user

        // Act
        val result = authService.login(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User is deactivated", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login - should fail with no credentials`() = runBlocking {
        // Arrange
        val request = UserLogin(
            email = null,
            username = null,
            password = testPassword
        )

        // Act
        val result = authService.login(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateToken - should return user with valid token`() = runBlocking {
        // Arrange
        val token = "valid.token"
        val user = createTestUser()
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            isExpired = false,
            error = null
        )

        coEvery { jwtTokenService.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns user

        // Act
        val result = authService.validateToken(token)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(testUserId, result.getOrNull()?.id)
    }

    @Test
    fun `validateToken - should fail with invalid token`() = runBlocking {
        // Arrange
        val token = "invalid.token"
        val validationResult = TokenValidationResult(
            isValid = false,
            userId = null,
            isExpired = false,
            error = "Invalid token"
        )

        coEvery { jwtTokenService.validateToken(token) } returns validationResult

        // Act
        val result = authService.validateToken(token)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Invalid token", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateToken - should fail when user not found`() = runBlocking {
        // Arrange
        val token = "valid.token"
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            isExpired = false,
            error = null
        )

        coEvery { jwtTokenService.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns null

        // Act
        val result = authService.validateToken(token)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateToken - should fail when user is deactivated`() = runBlocking {
        // Arrange
        val token = "valid.token"
        val user = createTestUser(isActive = false)
        val validationResult = TokenValidationResult(
            isValid = true,
            userId = testUserId,
            isExpired = false,
            error = null
        )

        coEvery { jwtTokenService.validateToken(token) } returns validationResult
        coEvery { userRepository.findById(testUserId) } returns user

        // Act
        val result = authService.validateToken(token)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User is deactivated", result.exceptionOrNull()?.message)
    }

    private fun createTestUser(
        id: UUID = testUserId,
        email: String = testEmail,
        username: String = testUsername,
        isEmailVerified: Boolean = true,
        isActive: Boolean = true
    ): User = User(
        id = id,
        email = email,
        username = username,
        passwordHash = testPasswordHash,
        firstName = "Test",
        lastName = "User",
        avatar = null,
        role = DomainUserRole.USER,
        isActive = isActive,
        isEmailVerified = isEmailVerified,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}