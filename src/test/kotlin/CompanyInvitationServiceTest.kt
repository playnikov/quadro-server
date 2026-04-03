package com.quadro

import com.quadro.datasource.repositories.company.CompanyInvitationRepository
import com.quadro.datasource.repositories.company.CompanyMemberRepository
import com.quadro.datasource.repositories.company.CompanyRepository
import com.quadro.datasource.repositories.users.UserRepository
import com.quadro.domain.models.company.Company
import com.quadro.domain.models.company.CompanyInvitation
import com.quadro.domain.models.company.CompanyMember
import com.quadro.domain.models.company.CompanyRole
import com.quadro.domain.models.company.CompanySettings
import com.quadro.domain.models.company.CompanyStatus
import com.quadro.domain.models.company.InvitationCreate
import com.quadro.domain.models.company.InvitationStatus
import com.quadro.domain.models.company.InvitationValidationResult
import com.quadro.domain.models.user.DomainUserRole
import com.quadro.domain.models.user.User
import com.quadro.domain.services.company.CompanyInvitationService
import com.quadro.domain.services.company.CompanyInvitationServiceImpl
import com.quadro.security.JwtInvitationTokenService
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

class CompanyInvitationServiceTest {
    private lateinit var companyRepository: CompanyRepository
    private lateinit var companyMemberRepository: CompanyMemberRepository
    private lateinit var companyInvitationRepository: CompanyInvitationRepository
    private lateinit var userRepository: UserRepository
    private lateinit var invitationTokenService: JwtInvitationTokenService
    private lateinit var invitationService: CompanyInvitationServiceImpl

    private val testCompanyId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testInvitationId = UUID.randomUUID()
    private val testTeamId = UUID.randomUUID()
    private val testToken = "test.jwt.token"
    private val testEmail = "inviter@test.com"
    private val testFirstName = "John"
    private val testLastName = "Doe"

    @Before
    fun setup() {
        companyRepository = mockk(relaxed = true)
        companyMemberRepository = mockk(relaxed = true)
        companyInvitationRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        invitationTokenService = mockk(relaxed = true)

        invitationService = CompanyInvitationServiceImpl(
            companyRepository = companyRepository,
            companyMemberRepository = companyMemberRepository,
            companyInvitationRepository = companyInvitationRepository,
            userRepository = userRepository,
            invitationTokenService = invitationTokenService
        )
    }

    // ============== Тесты создания приглашения ==============

    @Test
    fun `createInvitation - should create invitation successfully`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val inviter = createTestMember(role = CompanyRole.ADMIN)
        val inviterUser = createTestUser()
        val request = InvitationCreate(
            teamId = testTeamId,
            role = CompanyRole.MEMBER,
            message = "Join us!",
            expiresInDays = 7
        )

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns inviter
        coEvery { companyInvitationRepository.countPendingByCompany(testCompanyId) } returns 5
        coEvery { invitationTokenService.generateToken(any(), any(), any(), any()) } returns testToken
        coEvery { companyInvitationRepository.create(any()) } answers { firstArg() }
        coEvery { userRepository.findById(testUserId) } returns inviterUser

        // Act
        val result = invitationService.createInvitation(testCompanyId, testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        val invitationResult = result.getOrNull()
        assertNotNull(invitationResult)
        assertEquals(testCompanyId, invitationResult.companyId)
        assertEquals(testToken, invitationResult.token)
        assertEquals("http://127.0.0.1:8080/invite?token=$testToken", invitationResult.inviteLink)
        assertEquals(CompanyRole.MEMBER, invitationResult.role)

        coVerify(exactly = 1) { companyInvitationRepository.create(any()) }
    }

    @Test
    fun `createInvitation - should fail when company not found`() = runBlocking {
        // Arrange
        coEvery { companyRepository.findById(testCompanyId) } returns null

        // Act
        val result = invitationService.createInvitation(testCompanyId, testUserId, InvitationCreate())

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Company not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createInvitation - should fail with insufficient permissions`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val inviter = createTestMember(role = CompanyRole.MEMBER)

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns inviter

        // Act
        val result = invitationService.createInvitation(testCompanyId, testUserId, InvitationCreate())

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Insufficient permissions", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createInvitation - should use custom expiration days`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val inviter = createTestMember(role = CompanyRole.ADMIN)
        val inviterUser = createTestUser()
        val request = InvitationCreate(expiresInDays = 14)

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns inviter
        coEvery { companyInvitationRepository.countPendingByCompany(testCompanyId) } returns 0
        coEvery { invitationTokenService.generateToken(any(), any(), any(), eq(14)) } returns testToken
        coEvery { userRepository.findById(testUserId) } returns inviterUser

        // Act
        val result = invitationService.createInvitation(testCompanyId, testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { invitationTokenService.generateToken(any(), any(), any(), eq(14)) }
    }

    @Test
    fun `acceptInvitation - should accept invitation and add user to company`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val invitation = createTestInvitation(status = InvitationStatus.PENDING)
        val validationResult = InvitationValidationResult(
            isValid = true,
            invitationId = testInvitationId
        )

        coEvery { invitationTokenService.validateToken(testToken) } returns validationResult
        coEvery { companyInvitationRepository.findById(testInvitationId) } returns invitation
        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns false
        coEvery { companyMemberRepository.add(any()) } returns mockk()
        coEvery { companyInvitationRepository.acceptInvitation(testInvitationId, testUserId) } returns true

        // Act
        val result = invitationService.acceptInvitation(testUserId, testToken)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { companyMemberRepository.add(any()) }
        coVerify(exactly = 1) { companyInvitationRepository.acceptInvitation(testInvitationId, testUserId) }
    }

    @Test
    fun `acceptInvitation - should not add user if already in company`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val invitation = createTestInvitation(status = InvitationStatus.PENDING)
        val validationResult = InvitationValidationResult(
            isValid = true,
            invitationId = testInvitationId
        )

        coEvery { invitationTokenService.validateToken(testToken) } returns validationResult
        coEvery { companyInvitationRepository.findById(testInvitationId) } returns invitation
        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns true

        // Act
        val result = invitationService.acceptInvitation(testUserId, testToken)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { companyMemberRepository.add(any()) }
        coVerify(exactly = 0) { companyInvitationRepository.acceptInvitation(any(), any()) }
    }

    @Test
    fun `acceptInvitation - should fail with invalid token`() = runBlocking {
        // Arrange
        val validationResult = InvitationValidationResult(
            isValid = false,
            error = "Invalid token"
        )

        coEvery { invitationTokenService.validateToken("invalid") } returns validationResult

        // Act
        val result = invitationService.acceptInvitation(testUserId, "invalid")

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Invalid token", result.exceptionOrNull()?.message)
    }

    @Test
    fun `acceptInvitation - should fail when invitation expired`() = runBlocking {
        // Arrange
        val invitation = createTestInvitation(
            status = InvitationStatus.PENDING,
            expiresAt = System.currentTimeMillis() - 1000
        )
        val validationResult = InvitationValidationResult(
            isValid = true,
            invitationId = testInvitationId
        )

        coEvery { invitationTokenService.validateToken(testToken) } returns validationResult
        coEvery { companyInvitationRepository.findById(testInvitationId) } returns invitation

        // Act
        val result = invitationService.acceptInvitation(testUserId, testToken)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Invitation has expired", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { companyInvitationRepository.updateStatus(testInvitationId, InvitationStatus.EXPIRED) }
    }

    @Test
    fun `acceptInvitation - should fail when invitation not pending`() = runBlocking {
        // Arrange
        val invitation = createTestInvitation(status = InvitationStatus.ACCEPTED)
        val validationResult = InvitationValidationResult(
            isValid = true,
            invitationId = testInvitationId
        )

        coEvery { invitationTokenService.validateToken(testToken) } returns validationResult
        coEvery { companyInvitationRepository.findById(testInvitationId) } returns invitation

        // Act
        val result = invitationService.acceptInvitation(testUserId, testToken)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Invitation is no longer valid", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getInvitations - should return invitations for admin`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val member = createTestMember(role = CompanyRole.ADMIN)
        val invitations = listOf(
            createTestInvitation(id = UUID.randomUUID()),
            createTestInvitation(id = UUID.randomUUID())
        )
        val inviterUser = createTestUser()

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns member
        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyInvitationRepository.findByCompany(testCompanyId, null) } returns invitations
        coEvery { userRepository.findById(testUserId) } returns inviterUser

        // Act
        val result = invitationService.getInvitations(testCompanyId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getInvitations - should fail for member without permissions`() = runBlocking {
        // Arrange
        val member = createTestMember(role = CompanyRole.MEMBER)

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns member

        // Act
        val result = invitationService.getInvitations(testCompanyId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Insufficient permissions", result.exceptionOrNull()?.message)
    }

    @Test
    fun `cancelInvitation - should cancel pending invitation`() = runBlocking {
        // Arrange
        val member = createTestMember(role = CompanyRole.ADMIN)
        val invitation = createTestInvitation(status = InvitationStatus.PENDING)

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns member
        coEvery { companyInvitationRepository.findById(testInvitationId) } returns invitation
        coEvery { companyInvitationRepository.updateStatus(testInvitationId, InvitationStatus.CANCELLED) } returns true

        // Act
        val result = invitationService.cancelInvitation(testCompanyId, testUserId, testInvitationId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { companyInvitationRepository.updateStatus(testInvitationId, InvitationStatus.CANCELLED) }
    }

    @Test
    fun `cancelInvitation - should fail when invitation not pending`() = runBlocking {
        // Arrange
        val member = createTestMember(role = CompanyRole.ADMIN)
        val invitation = createTestInvitation(status = InvitationStatus.ACCEPTED)

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns member
        coEvery { companyInvitationRepository.findById(testInvitationId) } returns invitation

        // Act
        val result = invitationService.cancelInvitation(testCompanyId, testUserId, testInvitationId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Only pending invitations can be cancelled", result.exceptionOrNull()?.message)
    }

    @Test
    fun `resendInvitation - should resend pending invitation`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val member = createTestMember(role = CompanyRole.ADMIN)
        val invitation = createTestInvitation(status = InvitationStatus.PENDING)
        val inviterUser = createTestUser()
        val newToken = "new.jwt.token"

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns member
        coEvery { companyInvitationRepository.findById(testInvitationId) } returns invitation
        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { invitationTokenService.generateToken(any(), any(), any(), any()) } returns newToken
        coEvery { userRepository.findById(testUserId) } returns inviterUser

        // Act
        val result = invitationService.resendInvitation(testCompanyId, testUserId, testInvitationId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(newToken, result.getOrNull()?.token)
    }

    @Test
    fun `resendInvitation - should fail when invitation not pending`() = runBlocking {
        // Arrange
        val member = createTestMember(role = CompanyRole.ADMIN)
        val invitation = createTestInvitation(status = InvitationStatus.ACCEPTED)

        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns member
        coEvery { companyInvitationRepository.findById(testInvitationId) } returns invitation

        // Act
        val result = invitationService.resendInvitation(testCompanyId, testUserId, testInvitationId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Only pending invitations can be resent", result.exceptionOrNull()?.message)
    }

    // ============== Вспомогательные методы ==============

    private fun createTestCompany(): Company = Company(
        id = testCompanyId,
        name = "Test Company",
        description = null,
        logo = null,
        website = null,
        email = null,
        phone = null,
        address = null,
        taxId = null,
        companyStatus = CompanyStatus.ACTIVE,
        ownerId = testUserId,
        companySettings = CompanySettings(invitationExpiryDays = 7),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        deletedAt = null
    )

    private fun createTestMember(role: CompanyRole): CompanyMember = CompanyMember(
        id = UUID.randomUUID(),
        companyId = testCompanyId,
        userId = testUserId,
        role = role,
        joinedAt = System.currentTimeMillis(),
        invitedBy = testUserId,
        invitedAt = System.currentTimeMillis(),
        isActive = true
    )

    private fun createTestInvitation(
        id: UUID = testInvitationId,
        status: InvitationStatus = InvitationStatus.PENDING,
        expiresAt: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
    ): CompanyInvitation = CompanyInvitation(
        id = id,
        companyId = testCompanyId,
        teamId = testTeamId,
        invitedBy = testUserId,
        role = CompanyRole.MEMBER,
        status = status,
        token = testToken,
        expiresAt = expiresAt,
        createdAt = System.currentTimeMillis(),
        acceptedAt = null,
        message = "Test message",
        acceptedBy = null
    )

    private fun createTestUser(): com.quadro.domain.models.user.User = com.quadro.domain.models.user.User(
        id = testUserId,
        email = testEmail,
        username = "testuser",
        passwordHash = "hash",
        firstName = testFirstName,
        lastName = testLastName,
        role = DomainUserRole.USER,
        isEmailVerified = true,
        isActive = true,
        avatar = null
    )
}