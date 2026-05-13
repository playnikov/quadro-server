package com.quadro.auth

import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.domain.services.UserService
import com.quadro.auth.domain.services.UserServiceImpl
import com.quadro.shared.dto.DomainException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Clock

class UserServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    private val testUserId = UUID.randomUUID()
    private val testEmail = "test@example.com"
    private val testUsername = "testuser"
    private val testPasswordHash = "hashed"
    private val testFirstName = "Test"
    private val testLastName = "User"
    private val testMiddleName = "Testovich"

    @Before
    fun setUp() {
        userRepository = mockk()
        userService = UserServiceImpl(userRepository)
    }

    private fun createTestUser(
        id: UUID = testUserId,
        email: String = testEmail,
        username: String = testUsername,
        isActive: Boolean = true,
        role: UserRole = UserRole.USER
    ): User = User(
        id = id,
        username = username,
        email = email,
        passwordHash = testPasswordHash,
        firstName = testFirstName,
        lastName = testLastName,
        middleName = testMiddleName,
        avatarUrl = null,
        role = role,
        isActive = isActive,
        isEmailVerified = true,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now(),
        lastLoginAt = Clock.System.now(),
        lastLoginIp = "127.0.0.1"
    )

    @Test
    fun `getUserById - should return user when exists`() = runBlocking {
        // Arrange
        val expectedUser = createTestUser()
        coEvery { userRepository.findById(testUserId) } returns expectedUser

        // Act
        val result = userService.getUserById(testUserId)

        // Assert
        assertEquals(expectedUser, result)
        coVerify { userRepository.findById(testUserId) }
    }

    @Test
    fun `getUserById - should throw NotFound when user does not exist`() = runBlocking {
        // Arrange
        coEvery { userRepository.findById(testUserId) } returns null

        // Act & Assert
        val exception = assertFailsWith<DomainException.NotFound> {
            userService.getUserById(testUserId)
        }
        assertEquals("User with id '$testUserId' not found", exception.message)
        coVerify { userRepository.findById(testUserId) }
    }

    @Test
    fun `getAllUsers - should return list of users`() = runBlocking {
        // Arrange
        val users = listOf(
            createTestUser(id = UUID.randomUUID(), email = "user1@example.com"),
            createTestUser(id = UUID.randomUUID(), email = "user2@example.com")
        )
        coEvery { userRepository.getAll() } returns users

        // Act
        val result = userService.getAllUsers()

        // Assert
        assertEquals(users, result)
        assertEquals(2, result.size)
        coVerify { userRepository.getAll() }
    }

    @Test
    fun `getAllUsers - should return empty list when no users`() = runBlocking {
        // Arrange
        coEvery { userRepository.getAll() } returns emptyList()

        // Act
        val result = userService.getAllUsers()

        // Assert
        assertNotNull(result)
        assertEquals(0, result.size)
        coVerify { userRepository.getAll() }
    }

    @Test
    fun `getUserByUsername - should return user when exists`() = runBlocking {
        // Arrange
        val expectedUser = createTestUser()
        coEvery { userRepository.findByUsername(testUsername) } returns expectedUser

        // Act
        val result = userService.getUserByUsername(testUsername)

        // Assert
        assertEquals(expectedUser, result)
        coVerify { userRepository.findByUsername(testUsername) }
    }

    @Test
    fun `getUserByUsername - should throw NotFound when user does not exist`() = runBlocking {
        // Arrange
        coEvery { userRepository.findByUsername(testUsername) } returns null

        // Act & Assert
        val exception = assertFailsWith<DomainException.NotFound> {
            userService.getUserByUsername(testUsername)
        }
        assertEquals("User with id '$testUsername' not found", exception.message)
        coVerify { userRepository.findByUsername(testUsername) }
    }

    @Test
    fun `getUserByEmail - should return user when exists`() = runBlocking {
        // Arrange
        val expectedUser = createTestUser()
        coEvery { userRepository.findByEmail(testEmail) } returns expectedUser

        // Act
        val result = userService.getUserByEmail(testEmail)

        // Assert
        assertEquals(expectedUser, result)
        coVerify { userRepository.findByEmail(testEmail) }
    }

    @Test
    fun `getUserByEmail - should throw NotFound when user does not exist`() = runBlocking {
        // Arrange
        coEvery { userRepository.findByEmail(testEmail) } returns null

        // Act & Assert
        val exception = assertFailsWith<DomainException.NotFound> {
            userService.getUserByEmail(testEmail)
        }
        assertEquals("User with id '$testEmail' not found", exception.message)
        coVerify { userRepository.findByEmail(testEmail) }
    }

    @Test
    fun `getUsersByIds - should return users for existing ids`() = runBlocking {
        // Arrange
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val userIds = listOf(userId1, userId2)
        val expectedUsers = listOf(
            createTestUser(id = userId1, email = "user1@example.com"),
            createTestUser(id = userId2, email = "user2@example.com")
        )
        coEvery { userRepository.getByIds(userIds) } returns expectedUsers

        // Act
        val result = userService.getUsersByIds(userIds)

        // Assert
        assertEquals(expectedUsers, result)
        coVerify { userRepository.getByIds(userIds) }
    }

    @Test
    fun `getUsersByIds - should return empty list when no users found`() = runBlocking {
        // Arrange
        val userIds = listOf(UUID.randomUUID(), UUID.randomUUID())
        coEvery { userRepository.getByIds(userIds) } returns emptyList()

        // Act
        val result = userService.getUsersByIds(userIds)

        // Assert
        assertNotNull(result)
        assertEquals(0, result.size)
        coVerify { userRepository.getByIds(userIds) }
    }

    @Test
    fun `getUsersByIds - should return empty list for empty input list`() = runBlocking {
        // Arrange
        val userIds = emptyList<UUID>()
        coEvery { userRepository.getByIds(userIds) } returns emptyList()

        // Act
        val result = userService.getUsersByIds(userIds)

        // Assert
        assertEquals(emptyList<User>(), result)
        coVerify { userRepository.getByIds(userIds) }
    }
}