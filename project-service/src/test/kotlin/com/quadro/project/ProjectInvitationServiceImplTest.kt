package com.quadro.project

import com.quadro.project.domain.models.InvitationCreate
import com.quadro.project.domain.models.InviteStatus
import com.quadro.project.domain.models.InvitationValidationResult
import com.quadro.project.domain.models.InviteType
import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectInvitation
import com.quadro.project.domain.models.ProjectMember
import com.quadro.project.domain.models.ProjectPriority
import com.quadro.project.domain.models.MemberRole
import com.quadro.project.domain.models.ProjectStatus
import com.quadro.project.domain.models.ProjectType
import com.quadro.project.domain.models.ProjectVisibility
import com.quadro.project.domain.repositories.ProjectInvitationRepository
import com.quadro.project.domain.repositories.ProjectMemberRepository
import com.quadro.project.domain.repositories.ProjectRepository
import com.quadro.project.domain.services.InvitationTokenService
import com.quadro.project.domain.services.ProjectInvitationService
import com.quadro.project.domain.services.ProjectInvitationServiceImpl
import com.quadro.shared.data.config.DomainConfig
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.dto.DomainException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days


@OptIn(ExperimentalCoroutinesApi::class)
class ProjectInvitationServiceImplTest {
    private lateinit var projectInvitationRepository: ProjectInvitationRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var projectMemberRepository: ProjectMemberRepository
    private lateinit var invitationTokenService: InvitationTokenService
    private lateinit var eventProducer: EventProducer
    private lateinit var config: DomainConfig
    private lateinit var projectInvitationService: ProjectInvitationService

    private val testProjectId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testInvitationId = UUID.randomUUID()
    private val testToken = "test.jwt.token"
    private val now = Clock.System.now()

    private val testProject = Project(
        id = testProjectId,
        type = ProjectType.TEAM_MANAGED,
        name = "Test Project",
        key = "TP",
        description = null,
        status = ProjectStatus.ACTIVE,
        priority = ProjectPriority.MEDIUM,
        visibility = ProjectVisibility.PUBLIC,
        createdAt = now,
        updatedAt = now
    )

    private val testMember = ProjectMember(
        id = UUID.randomUUID(),
        projectId = testProjectId,
        userId = testUserId,
        role = MemberRole.OWNER,
        joinedAt = now,
        invitedBy = testUserId,
        invitedAt = now
    )

    private val testInvitation = ProjectInvitation(
        id = testInvitationId,
        projectId = testProjectId,
        invitedBy = testUserId,
        type = InviteType.EMAIL,
        identifier = "user@example.com",
        role = MemberRole.MEMBER,
        status = InviteStatus.PENDING,
        token = testToken,
        expiresAt = now.plus(7.days),
        createdAt = now,
        acceptedAt = null,
        acceptedBy = null,
        message = "Join us!"
    )

    @Before
    fun setUp() {
        projectInvitationRepository = mockk(relaxed = true)
        projectRepository = mockk(relaxed = true)
        projectMemberRepository = mockk(relaxed = true)
        invitationTokenService = mockk(relaxed = true)
        eventProducer = mockk(relaxed = true)
        config = mockk {
            every { domain } returns "https://quadro.com"
        }
        projectInvitationService = ProjectInvitationServiceImpl(
            projectInvitationRepository = projectInvitationRepository,
            projectRepository = projectRepository,
            projectMemberRepository = projectMemberRepository,
            invitationTokenService = invitationTokenService,
            eventProducer = eventProducer,
            config = config
        )
    }

    @Test
    fun `createInvitation - success by OWNER`() = runTest {
        val request = InvitationCreate(
            inviteType = InviteType.EMAIL,
            identifier = "new@example.com",
            role = MemberRole.MEMBER,
            expiresInDays = 5,
            message = "Welcome"
        )
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns testMember
        coEvery { projectInvitationRepository.countPendingByProject(testProjectId) } returns 0
        coEvery { invitationTokenService.generateToken(any(), any(), any()) } returns testToken

        val result = projectInvitationService.createInvitation(testProjectId, testUserId, request)

        assertNotNull(result)
        assertEquals(testProject.name, result.project.name)
        assertEquals(testToken, result.token)
        assertTrue(result.link?.contains(testToken) ?: true)
        coVerify { projectInvitationRepository.create(any()) }
    }

    @Test
    fun `createInvitation - fails when project not found`() = runTest {
        val request = mockk<InvitationCreate>()
        coEvery { projectRepository.findById(testProjectId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            projectInvitationService.createInvitation(testProjectId, testUserId, request)
        }
        assertEquals("Project with id '$testProjectId' not found", ex.message)
    }

    @Test
    fun `createInvitation - fails when user is not a member or has insufficient role`() = runTest {
        val request = mockk<InvitationCreate>()
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns null

        val ex = assertFailsWith<DomainException.AccessDenied> {
            projectInvitationService.createInvitation(testProjectId, testUserId, request)
        }
        assertEquals("Insufficient permissions", ex.message)
    }

    @Test
    fun `createInvitation - fails when pending invitations exceed limit`() = runTest {
        val request = mockk<InvitationCreate>()
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns testMember
        coEvery { projectInvitationRepository.countPendingByProject(testProjectId) } returns 50

        val ex = assertFailsWith<DomainException.BusinessRule> {
            projectInvitationService.createInvitation(testProjectId, testUserId, request)
        }
        assertEquals("Too many pending invitations", ex.message)
    }

    // ==================== acceptInvitation ====================

    @Test
    fun `acceptInvitation - success and adds member`() = runTest {
        val validation = InvitationValidationResult(
            isValid = true,
            invitationId = testInvitationId,
            projectId = testProjectId,
            expiresAt = now.plus(1.days).toEpochMilliseconds()
        )
        coEvery { invitationTokenService.validateToken(testToken) } returns validation
        coEvery { projectInvitationRepository.findById(testInvitationId) } returns testInvitation
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns false

        val result = projectInvitationService.acceptInvitation(testToken, testUserId)

        assertEquals(testProjectId.toString(), result.id)
    }

    @Test
    fun `acceptInvitation - invalid token`() = runTest {
        val validation = InvitationValidationResult(isValid = false, error = "Token expired")
        coEvery { invitationTokenService.validateToken(testToken) } returns validation

        val ex = assertFailsWith<DomainException.ValidationError> {
            projectInvitationService.acceptInvitation(testToken, testUserId)
        }
        assertEquals("Token expired", ex.message)
    }

    @Test
    fun `acceptInvitation - invitation already accepted`() = runTest {
        val validation = InvitationValidationResult(isValid = true, invitationId = testInvitationId, projectId = testProjectId)
        val expiredInvitation = testInvitation.copy(status = InviteStatus.ACCEPTED)
        coEvery { invitationTokenService.validateToken(testToken) } returns validation
        coEvery { projectInvitationRepository.findById(testInvitationId) } returns expiredInvitation

        val ex = assertFailsWith<DomainException.BusinessRule> {
            projectInvitationService.acceptInvitation(testToken, testUserId)
        }
        assertEquals("Invitation is no longer valid", ex.message)
    }

    @Test
    fun `acceptInvitation - invitation expired by time`() = runTest {
        val validation = InvitationValidationResult(isValid = true, invitationId = testInvitationId, projectId = testProjectId)
        val expiredInvitation = testInvitation.copy(expiresAt = now.minus(1.days))
        coEvery { invitationTokenService.validateToken(testToken) } returns validation
        coEvery { projectInvitationRepository.findById(testInvitationId) } returns expiredInvitation

        val ex = assertFailsWith<DomainException.BusinessRule> {
            projectInvitationService.acceptInvitation(testToken, testUserId)
        }
        assertEquals("Invitation has expired", ex.message)
    }

    // ==================== getInvitations ====================

    @Test
    fun `getInvitations - success as ADMIN`() = runTest {
        val adminMember = testMember.copy(role = MemberRole.ADMIN)
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns adminMember
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectInvitationRepository.findByProject(testProjectId, null) } returns listOf(testInvitation)

        val result = projectInvitationService.getInvitations(testProjectId, testUserId)

        assertEquals(1, result.size)
        assertEquals(testInvitation.id.toString(), result.first().id)
    }

    @Test
    fun `getInvitations - fails for non-member`() = runTest {
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns null

        val ex = assertFailsWith<DomainException.AccessDenied> {
            projectInvitationService.getInvitations(testProjectId, testUserId)
        }
        assertEquals("Insufficient permissions", ex.message)
    }

    // ==================== cancelInvitation ====================

    @Test
    fun `cancelInvitation - success`() = runTest {
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns testMember
        coEvery { projectInvitationRepository.findById(testInvitationId) } returns testInvitation

        projectInvitationService.cancelInvitation(testProjectId, testUserId, testInvitationId)

        coVerify { projectInvitationRepository.updateStatus(testInvitationId, InviteStatus.CANCELLED) }
    }

    @Test
    fun `cancelInvitation - fails when invitation belongs to different project`() = runTest {
        val otherProjectInvitation = testInvitation.copy(projectId = UUID.randomUUID())
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns testMember
        coEvery { projectInvitationRepository.findById(testInvitationId) } returns otherProjectInvitation

        val ex = assertFailsWith<DomainException.BusinessRule> {
            projectInvitationService.cancelInvitation(testProjectId, testUserId, testInvitationId)
        }
        assertEquals("Invitation does not belong to this project", ex.message)
    }
}