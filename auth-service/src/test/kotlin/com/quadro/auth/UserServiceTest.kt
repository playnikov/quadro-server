package com.quadro.auth

import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.domain.services.UserService
import com.quadro.auth.domain.services.UserServiceImpl
import com.quadro.auth.infrastructure.security.PasswordEncoder
import com.quadro.auth.presentation.models.UpdateAdminUserRequest
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.events.UserUpdatedEvent
import com.quadro.shared.dto.DomainException
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.*
import kotlin.time.Clock

class UserServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var eventProducer: EventProducer
    private lateinit var userService: UserService

    private val testUserId = UUID.randomUUID()
    private val testAdminId = UUID.randomUUID()
    private val testSuperAdminId = UUID.randomUUID()
    private val testEmail = "test@example.com"
    private val testUsername = "testuser"
    private val testPassword = "Password123"
    private val testPasswordHash = "hashed_password"
    private val testFirstName = "Test"
    private val testLastName = "User"
    private val testMiddleName = "Testovich"

    @Before
    fun setUp() {
        userRepository = mockk(relaxed = true)
        passwordEncoder = mockk(relaxed = true)
        eventProducer = mockk(relaxed = true)
        userService = UserServiceImpl(userRepository, passwordEncoder, eventProducer)
    }

    private fun createTestUser(
        id: UUID = testUserId,
        email: String = testEmail,
        username: String = testUsername,
        isActive: Boolean = true,
        role: UserRole = UserRole.USER,
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
        role = role,
        isActive = isActive,
        isEmailVerified = true,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now(),
        lastLoginAt = Clock.System.now()
    )

    // ================================ GET USER BY ID =================================

    @Test
    fun `getUserById - success`() = runBlocking {
        val expectedUser = createTestUser()
        coEvery { userRepository.findById(testUserId) } returns expectedUser

        val result = userService.getUserById(testUserId)

        assertEquals(expectedUser, result)
        coVerify { userRepository.findById(testUserId) }
    }

    @Test
    fun `getUserById - throws NotFound when user missing`() = runBlocking {
        coEvery { userRepository.findById(testUserId) } returns null

        val exception = assertFailsWith<DomainException.NotFound> {
            userService.getUserById(testUserId)
        }
        assertEquals("User with '$testUserId' not found", exception.message)
    }

    // ================================ GET ALL USERS (admin only) =================================

    @Test
    fun `getAllUsers - success for admin`() = runBlocking {
        val adminUser = createTestUser(id = testAdminId, role = UserRole.ADMIN)
        val users = listOf(
            createTestUser(id = UUID.randomUUID()),
            createTestUser(id = UUID.randomUUID())
        )
        coEvery { userRepository.findById(testAdminId) } returns adminUser
        coEvery { userRepository.getAll() } returns users

        val result = userService.getAllUsers(testAdminId)

        assertEquals(users, result)
        coVerify { userRepository.getAll() }
    }

    @Test
    fun `getAllUsers - throws AccessDenied for non-admin`() = runBlocking {
        val regularUser = createTestUser(id = testUserId, role = UserRole.USER)
        coEvery { userRepository.findById(testUserId) } returns regularUser

        val exception = assertFailsWith<DomainException.AccessDenied> {
            userService.getAllUsers(testUserId)
        }
        coVerify(exactly = 0) { userRepository.getAll() }
    }

    @Test
    fun `getAllUsers - throws NotFound when requester missing`() = runBlocking {
        coEvery { userRepository.findById(testAdminId) } returns null

        val exception = assertFailsWith<DomainException.NotFound> {
            userService.getAllUsers(testAdminId)
        }
        assertEquals("User with '$testAdminId' not found", exception.message)
    }

    // ================================ GET USER BY USERNAME =================================

    @Test
    fun `getUserByUsername - success`() = runBlocking {
        val user = createTestUser()
        coEvery { userRepository.findByUsername(testUsername) } returns user

        val result = userService.getUserByUsername(testUsername)

        assertEquals(user, result)
        coVerify { userRepository.findByUsername(testUsername) }
    }

    @Test
    fun `getUserByUsername - throws NotFound`() = runBlocking {
        coEvery { userRepository.findByUsername(testUsername) } returns null

        val exception = assertFailsWith<DomainException.NotFound> {
            userService.getUserByUsername(testUsername)
        }
        assertEquals("User with '$testUsername' not found", exception.message)
    }

    // ================================ GET USER BY EMAIL =================================

    @Test
    fun `getUserByEmail - success`() = runBlocking {
        val user = createTestUser()
        coEvery { userRepository.findByEmail(testEmail) } returns user

        val result = userService.getUserByEmail(testEmail)

        assertEquals(user, result)
        coVerify { userRepository.findByEmail(testEmail) }
    }

    @Test
    fun `getUserByEmail - throws NotFound`() = runBlocking {
        coEvery { userRepository.findByEmail(testEmail) } returns null

        val exception = assertFailsWith<DomainException.NotFound> {
            userService.getUserByEmail(testEmail)
        }
        assertEquals("User with '$testEmail' not found", exception.message)
    }

    // ================================ GET USERS BY IDS =================================

    @Test
    fun `getUsersByIds - success`() = runBlocking {
        val ids = listOf(UUID.randomUUID(), UUID.randomUUID())
        val users = ids.map { createTestUser(id = it) }
        coEvery { userRepository.getByIds(ids) } returns users

        val result = userService.getUsersByIds(ids)

        assertEquals(users, result)
        coVerify { userRepository.getByIds(ids) }
    }

    @Test
    fun `getUsersByIds - returns empty list when none found`() = runBlocking {
        val ids = listOf(UUID.randomUUID())
        coEvery { userRepository.getByIds(ids) } returns emptyList()

        val result = userService.getUsersByIds(ids)

        assertTrue(result.isEmpty())
    }

    // ================================ UPDATE USER BY ADMIN =================================

    @Test
    fun `updateUserByAdmin - admin updates another user successfully`() = runBlocking {
        val adminUser = createTestUser(id = testAdminId, role = UserRole.ADMIN)
        val targetUser = createTestUser(id = testUserId, role = UserRole.USER)
        val updateRequest = UpdateAdminUserRequest(
            firstName = "Updated",
            lastName = "Name",
            isActive = false
        )

        coEvery { userRepository.findById(testAdminId) } returns adminUser
        coEvery { userRepository.findById(testUserId) } returns targetUser
        coEvery { userRepository.upsert(any()) } answers { firstArg() }

        val result = userService.updateUserByAdmin(testAdminId, testUserId, updateRequest)

        assertEquals("Updated", result.firstName)
        assertEquals("Name", result.lastName)
        assertFalse(result.isActive)
    }

    @Test
    fun `updateUserByAdmin - admin cannot change role to ADMIN without superadmin rights`() = runBlocking {
        val adminUser = createTestUser(id = testAdminId, role = UserRole.ADMIN)
        val targetUser = createTestUser(id = testUserId, role = UserRole.USER)
        val updateRequest = UpdateAdminUserRequest(role = UserRole.ADMIN)

        coEvery { userRepository.findById(testAdminId) } returns adminUser
        coEvery { userRepository.findById(testUserId) } returns targetUser

        val exception = assertFailsWith<DomainException.AccessDenied> {
            userService.updateUserByAdmin(testAdminId, testUserId, updateRequest)
        }
        coVerify(exactly = 0) { userRepository.upsert(any()) }
    }

    @Test
    fun `updateUserByAdmin - superadmin can change role`() = runBlocking {
        val superAdmin = createTestUser(id = testSuperAdminId, role = UserRole.SUPER_ADMIN)
        val targetUser = createTestUser(id = testUserId, role = UserRole.USER)
        val updateRequest = UpdateAdminUserRequest(role = UserRole.ADMIN)

        coEvery { userRepository.findById(testSuperAdminId) } returns superAdmin
        coEvery { userRepository.findById(testUserId) } returns targetUser
        coEvery { userRepository.upsert(any()) } answers { firstArg() }

        val result = userService.updateUserByAdmin(testSuperAdminId, testUserId, updateRequest)

        assertEquals(UserRole.ADMIN, result.role)
    }

    @Test
    fun `updateUserByAdmin - admin cannot update themselves`() = runBlocking {
        val adminUser = createTestUser(id = testAdminId, role = UserRole.ADMIN)
        coEvery { userRepository.findById(testAdminId) } returns adminUser

        val exception = assertFailsWith<DomainException.AccessDenied> {
            userService.updateUserByAdmin(testAdminId, testAdminId, UpdateAdminUserRequest(firstName = "New"))
        }
        coVerify(exactly = 0) { userRepository.upsert(any()) }
    }

    @Test
    fun `updateUserByAdmin - requester not found throws NotFound`() = runBlocking {
        coEvery { userRepository.findById(testAdminId) } returns null

        val exception = assertFailsWith<DomainException.NotFound> {
            userService.updateUserByAdmin(testAdminId, testUserId, UpdateAdminUserRequest())
        }
        assertEquals("User with '$testAdminId' not found", exception.message)
    }

    @Test
    fun `updateUserByAdmin - target user not found throws NotFound`() = runBlocking {
        val adminUser = createTestUser(id = testAdminId, role = UserRole.ADMIN)
        coEvery { userRepository.findById(testAdminId) } returns adminUser
        coEvery { userRepository.findById(testUserId) } returns null

        val exception = assertFailsWith<DomainException.NotFound> {
            userService.updateUserByAdmin(testAdminId, testUserId, UpdateAdminUserRequest())
        }
        assertEquals("User with '$testUserId' not found", exception.message)
    }

    @Test
    fun `updateUserByAdmin - invalid email format throws ValidationError`() = runBlocking {
        val adminUser = createTestUser(id = testAdminId, role = UserRole.ADMIN)
        val targetUser = createTestUser(id = testUserId)
        coEvery { userRepository.findById(testAdminId) } returns adminUser
        coEvery { userRepository.findById(testUserId) } returns targetUser

        val exception = assertFailsWith<DomainException.ValidationError> {
            userService.updateUserByAdmin(testAdminId, testUserId, UpdateAdminUserRequest(email = "invalid-email"))
        }
        assertEquals("Invalid email format", exception.message)
    }

    @Test
    fun `updateUserByAdmin - password update triggers isNeedChangePassword = true`() = runBlocking {
        val adminUser = createTestUser(id = testAdminId, role = UserRole.ADMIN)
        val targetUser = createTestUser(id = testUserId)
        val newPassword = "NewPass123"
        val newHash = "new_hashed"

        coEvery { userRepository.findById(testAdminId) } returns adminUser
        coEvery { userRepository.findById(testUserId) } returns targetUser
        coEvery { passwordEncoder.encode(newPassword) } returns newHash
        coEvery { userRepository.upsert(any()) } answers { firstArg() }

        val result = userService.updateUserByAdmin(testAdminId, testUserId, UpdateAdminUserRequest(password = newPassword))

        assertEquals(newHash, result.passwordHash)
        assertTrue(result.isNeedChangePassword)
        coVerify { passwordEncoder.encode(newPassword) }
    }

    // ================================ ADMIN CREATE USER =================================

    @Test
    fun `adminCreateUser - admin creates user successfully`() = runBlocking {
        val adminUser = createTestUser(id = testAdminId, role = UserRole.ADMIN)
        val createRequest = UserCreate(
            email = "new@example.com",
            username = "newuser",
            password = testPassword,
            firstName = "New",
            lastName = "User",
            middleName = null
        )

        coEvery { userRepository.findById(testAdminId) } returns adminUser
        coEvery { passwordEncoder.encode(testPassword) } returns "new_hash"
        coEvery { userRepository.upsert(any()) } answers { firstArg() }

        val result = userService.adminCreateUser(testAdminId, createRequest)

        assertEquals("new@example.com", result.email)
        assertEquals("newuser", result.username)
        assertEquals(UserRole.USER, result.role)
        assertTrue(result.isNeedChangePassword)
    }

    @Test
    fun `adminCreateUser - invalid registration data throws ValidationError`() = runBlocking {
        val adminUser = createTestUser(id = testAdminId, role = UserRole.ADMIN)
        coEvery { userRepository.findById(testAdminId) } returns adminUser

        val invalidRequest = UserCreate(
            email = "invalid",
            username = "ab",
            password = "short",
            firstName = "New",
            lastName = "User",
            middleName = null
        )

        val exception = assertFailsWith<DomainException.ValidationError> {
            userService.adminCreateUser(testAdminId, invalidRequest)
        }
        assertEquals("Invalid email format", exception.message)
        coVerify(exactly = 0) { userRepository.upsert(any()) }
    }
}