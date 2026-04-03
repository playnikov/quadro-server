package com.quadro

import com.quadro.datasource.repositories.company.CompanyInvitationRepository
import com.quadro.datasource.repositories.company.CompanyMemberRepository
import com.quadro.datasource.repositories.company.CompanyRepository
import com.quadro.datasource.repositories.users.UserRepository
import com.quadro.domain.models.company.Company
import com.quadro.domain.models.company.CompanyCreate
import com.quadro.domain.models.company.CompanyMember
import com.quadro.domain.models.company.CompanyResult
import com.quadro.domain.models.company.CompanyRole
import com.quadro.domain.models.company.CompanySettings
import com.quadro.domain.models.company.CompanyStatus
import com.quadro.domain.models.company.CompanyUpdate
import com.quadro.domain.models.company.UpdateCompanyMemberRole
import com.quadro.domain.models.user.DomainUserRole
import com.quadro.domain.services.company.CompanyService
import com.quadro.domain.services.company.CompanyServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CompanyServiceTest {
    private lateinit var companyRepository: CompanyRepository
    private lateinit var companyMemberRepository: CompanyMemberRepository
    private lateinit var companyInvitationRepository: CompanyInvitationRepository
    private lateinit var userRepository: UserRepository
    private lateinit var companyService: CompanyServiceImpl

    private val testCompanyId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testTargetUserId = UUID.randomUUID()
    private val testMemberId = UUID.randomUUID()
    private val testEmail = "user@test.com"
    private val testUsername = "testuser"

    @Before
    fun setup() {
        companyRepository = mockk(relaxed = true)
        companyMemberRepository = mockk(relaxed = true)
        companyInvitationRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)

        companyService = CompanyServiceImpl(
            companyRepository = companyRepository,
            companyMemberRepository = companyMemberRepository,
            companyInvitationRepository = companyInvitationRepository,
            userRepository = userRepository
        )
    }

    @Test
    fun `createCompany - should create company successfully`() = runBlocking {
        // Arrange
        val request = CompanyCreate(
            name = "Test Company",
            description = "Test Description",
            website = "https://test.com"
        )

        coEvery { companyRepository.existsByName("Test Company") } returns false
        coEvery { companyRepository.create(any()) } answers { firstArg() }
        coEvery { companyMemberRepository.add(any()) } returns mockk()

        // Act
        val result = companyService.createCompany(testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        val companyResult = result.getOrNull()
        assertEquals("Test Company", companyResult?.name)
        assertEquals("Test Description", companyResult?.description)

        coVerify(exactly = 1) { companyRepository.create(any()) }
        coVerify(exactly = 1) { companyMemberRepository.add(any()) }
    }

    @Test
    fun `createCompany - should fail when company name already exists`() = runBlocking {
        // Arrange
        val request = CompanyCreate(name = "Test Company")

        coEvery { companyRepository.existsByName("Test Company") } returns true

        // Act
        val result = companyService.createCompany(testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Company with this name already exists", result.exceptionOrNull()?.message)

        coVerify(exactly = 0) { companyRepository.create(any()) }
    }

    @Test
    fun `createCompany - should add creator as owner`() = runBlocking {
        // Arrange
        val request = CompanyCreate(name = "Test Company")

        coEvery { companyRepository.existsByName("Test Company") } returns false
        coEvery { companyRepository.create(any()) } answers { firstArg() }

        // Act
        val result = companyService.createCompany(testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            companyMemberRepository.add(match { member ->
                member.role == CompanyRole.OWNER && member.userId == testUserId
            })
        }
    }

    @Test
    fun `getCompany - should return company when user has access`() = runBlocking {
        // Arrange
        val company = createTestCompany()

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns true

        // Act
        val result = companyService.getCompany(testCompanyId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(testCompanyId.toString(), result.getOrNull()?.id)
    }

    @Test
    fun `getCompany - should fail when company not found`() = runBlocking {
        // Arrange
        coEvery { companyRepository.findById(testCompanyId) } returns null

        // Act
        val result = companyService.getCompany(testCompanyId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Company not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCompany - should fail when user has no access`() = runBlocking {
        // Arrange
        val company = createTestCompany()

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns false

        // Act
        val result = companyService.getCompany(testCompanyId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Access denied", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateCompany - should update company when user is admin`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val member = createTestMember(role = CompanyRole.ADMIN)
        val request = CompanyUpdate(
            name = "Updated Name",
            description = "Updated Description"
        )

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns member
        coEvery { companyRepository.existsByName("Updated Name") } returns false
        coEvery { companyRepository.update(any()) } answers { firstArg() }

        // Act
        val result = companyService.updateCompany(testCompanyId, testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Updated Name", result.getOrNull()?.name)

        coVerify(exactly = 1) { companyRepository.update(any()) }
    }

    @Test
    fun `updateCompany - should fail when user has insufficient permissions`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val member = createTestMember(role = CompanyRole.MEMBER) // MEMBER не может обновлять

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns member

        // Act
        val result = companyService.updateCompany(testCompanyId, testUserId, CompanyUpdate())

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Insufficient permissions", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateCompany - should fail when new name already exists`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val member = createTestMember(role = CompanyRole.ADMIN)
        val request = CompanyUpdate(name = "Existing Name")

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns member
        coEvery { companyRepository.existsByName("Existing Name") } returns true

        // Act
        val result = companyService.updateCompany(testCompanyId, testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Company with this name already exists", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteCompany - should delete company when user is owner`() = runBlocking {
        // Arrange
        val company = createTestCompany(ownerId = testUserId)

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyRepository.updateStatus(testCompanyId, CompanyStatus.CLOSED) } returns true

        // Act
        val result = companyService.deleteCompany(testCompanyId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { companyRepository.updateStatus(testCompanyId, CompanyStatus.CLOSED) }
    }

    @Test
    fun `deleteCompany - should fail when user is not owner`() = runBlocking {
        // Arrange
        val company = createTestCompany(ownerId = UUID.randomUUID()) // Другой владелец

        coEvery { companyRepository.findById(testCompanyId) } returns company

        // Act
        val result = companyService.deleteCompany(testCompanyId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Only owner can delete company", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserCompanies - should return user companies`() = runBlocking {
        // Arrange
        val companies = listOf(
            createTestCompany(id = UUID.randomUUID()),
            createTestCompany(id = UUID.randomUUID())
        )

        coEvery { companyRepository.findByUser(testUserId, 10, 0) } returns companies

        // Act
        val result = companyService.getUserCompanies(testUserId, 1, 10)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getUserCompanies - should handle pagination`() = runBlocking {
        // Arrange
        coEvery { companyRepository.findByUser(testUserId, 5, 10) } returns emptyList()

        // Act
        companyService.getUserCompanies(testUserId, 3, 5) // page 3, size 5, offset = 10

        // Assert
        coVerify(exactly = 1) { companyRepository.findByUser(testUserId, 5, 10) }
    }

    @Test
    fun `getCompanyMembers - should return members list`() = runBlocking {
        // Arrange
        val members = listOf(
            createTestMember(id = UUID.randomUUID(), userId = UUID.randomUUID()),
            createTestMember(id = UUID.randomUUID(), userId = UUID.randomUUID())
        )
        val user = createTestUser()
        val inviter = createTestUser()

        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns true
        coEvery { companyMemberRepository.findByCompany(testCompanyId, 20, 0) } returns members
        coEvery { userRepository.findById(any()) } returns user
        coEvery { userRepository.findById(testUserId) } returns inviter

        // Act
        val result = companyService.getCompanyMembers(testCompanyId, testUserId, 1, 20)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getCompanyMembers - should fail when user has no access`() = runBlocking {
        // Arrange
        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns false

        // Act
        val result = companyService.getCompanyMembers(testCompanyId, testUserId, 1, 20)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Access denied", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateMemberRole - should update role when user is owner`() = runBlocking {
        // Arrange
        val currentUser = createTestMember(role = CompanyRole.OWNER)
        val targetUser = createTestMember(id = testMemberId, userId = testTargetUserId, role = CompanyRole.MEMBER)
        val request = UpdateCompanyMemberRole(role = CompanyRole.ADMIN)

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns currentUser
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testTargetUserId) } returns targetUser
        coEvery { companyMemberRepository.updateRole(testMemberId, CompanyRole.ADMIN) } returns true

        // Act
        val result = companyService.updateMemberRole(testCompanyId, testUserId, testTargetUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { companyMemberRepository.updateRole(testMemberId, CompanyRole.ADMIN) }
    }

    @Test
    fun `updateMemberRole - should fail when admin tries to change another admin`() = runBlocking {
        // Arrange
        val currentUser = createTestMember(role = CompanyRole.ADMIN)
        val targetUser = createTestMember(role = CompanyRole.ADMIN)
        val request = UpdateCompanyMemberRole(role = CompanyRole.MEMBER)

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns currentUser
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testTargetUserId) } returns targetUser

        // Act
        val result = companyService.updateMemberRole(testCompanyId, testUserId, testTargetUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Admin cannot change another admin's role", result.exceptionOrNull()?.message)
    }

    @Test
    fun `removeMember - should remove member when user is owner`() = runBlocking {
        // Arrange
        val currentUser = createTestMember(role = CompanyRole.OWNER)
        val targetUser = createTestMember(id = testMemberId, role = CompanyRole.MEMBER)

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns currentUser
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testTargetUserId) } returns targetUser
        coEvery { companyMemberRepository.remove(testMemberId) } returns true

        // Act
        val result = companyService.removeMember(testCompanyId, testUserId, testTargetUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { companyMemberRepository.remove(testMemberId) }
    }

    @Test
    fun `removeMember - should fail when trying to remove owner`() = runBlocking {
        // Arrange
        val currentUser = createTestMember(role = CompanyRole.OWNER)
        val targetUser = createTestMember(role = CompanyRole.OWNER)

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns currentUser
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testTargetUserId) } returns targetUser

        // Act
        val result = companyService.removeMember(testCompanyId, testUserId, testTargetUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Cannot remove owner", result.exceptionOrNull()?.message)
    }

    @Test
    fun `leaveCompany - should allow member to leave`() = runBlocking {
        // Arrange
        val member = createTestMember(id = testMemberId, role = CompanyRole.MEMBER)

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns member
        coEvery { companyMemberRepository.remove(testMemberId) } returns true

        // Act
        val result = companyService.leaveCompany(testCompanyId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { companyMemberRepository.remove(testMemberId) }
    }

    @Test
    fun `leaveCompany - should fail when owner tries to leave`() = runBlocking {
        // Arrange
        val member = createTestMember(role = CompanyRole.OWNER)

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns member

        // Act
        val result = companyService.leaveCompany(testCompanyId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Owner cannot leave the company. Transfer ownership first or delete the company.",
            result.exceptionOrNull()?.message)
    }

    private fun createTestCompany(
        id: UUID = testCompanyId,
        ownerId: UUID = testUserId
    ): Company = Company(
        id = id,
        name = "Test Company",
        description = "Description",
        logo = null,
        website = null,
        email = null,
        phone = null,
        address = null,
        taxId = null,
        companyStatus = CompanyStatus.ACTIVE,
        ownerId = ownerId,
        companySettings = CompanySettings(),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        deletedAt = null
    )

    private fun createTestMember(
        id: UUID = testMemberId,
        userId: UUID = testUserId,
        role: CompanyRole = CompanyRole.MEMBER
    ): CompanyMember = CompanyMember(
        id = id,
        companyId = testCompanyId,
        userId = userId,
        role = role,
        joinedAt = System.currentTimeMillis(),
        invitedBy = testUserId,
        invitedAt = System.currentTimeMillis(),
        isActive = true
    )

    private fun createTestUser(): com.quadro.domain.models.user.User = com.quadro.domain.models.user.User(
        id = testUserId,
        email = testEmail,
        username = testUsername,
        passwordHash = "hash",
        firstName = "Test",
        lastName = "User",
        role = DomainUserRole.USER,
        isEmailVerified = true,
        isActive = true,
        avatar = null
    )
}