package com.quadro.project

import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectCreate
import com.quadro.project.domain.models.ProjectMember
import com.quadro.project.domain.models.ProjectPriority
import com.quadro.project.domain.models.ProjectRole
import com.quadro.project.domain.models.ProjectStatus
import com.quadro.project.domain.models.ProjectType
import com.quadro.project.domain.models.ProjectUpdate
import com.quadro.project.domain.models.ProjectVisibility
import com.quadro.project.domain.models.User
import com.quadro.project.domain.models.UserRole
import com.quadro.project.domain.repositories.ProjectMemberRepository
import com.quadro.project.domain.repositories.ProjectRepository
import com.quadro.project.domain.repositories.UserRepository
import com.quadro.project.domain.services.ProjectServiceImpl
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.dto.DomainException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectServiceImplTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var userRepository: UserRepository
    private lateinit var projectMemberRepository: ProjectMemberRepository
    private lateinit var eventProducer: EventProducer
    private lateinit var projectService: ProjectServiceImpl

    private val testUserId = UUID.randomUUID()
    private val testProjectId = UUID.randomUUID()
    private val testMemberId = UUID.randomUUID()
    private val now = Clock.System.now()

    private val testUser = User(
        id = testUserId,
        role = UserRole.USER,
        isActive = true
    )

    private val testProject = Project(
        id = testProjectId,
        type = ProjectType.TEAM_MANAGED,
        name = "Test Project",
        key = "TP",
        description = "Description",
        status = ProjectStatus.ACTIVE,
        priority = ProjectPriority.MEDIUM,
        visibility = ProjectVisibility.PUBLIC,
        createdAt = now,
        updatedAt = now
    )

    private val testMember = ProjectMember(
        id = testMemberId,
        projectId = testProjectId,
        userId = testUserId,
        role = ProjectRole.OWNER,
        joinedAt = now,
        invitedBy = testUserId,
        invitedAt = now
    )

    @Before
    fun setUp() {
        projectRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        projectMemberRepository = mockk(relaxed = true)
        eventProducer = mockk(relaxed = true)
        projectService = ProjectServiceImpl(
            projectRepository,
            userRepository,
            projectMemberRepository,
            eventProducer
        )
    }

    @Test
    fun `createProject - success as ADMIN`() = runBlocking {
        val adminUser = testUser.copy(role = UserRole.ADMIN)
        val request = ProjectCreate(
            type = ProjectType.TEAM_MANAGED,
            name = "Admin Project",
            key = "AP",
            description = null,
            priority = ProjectPriority.MEDIUM,
            visibility = ProjectVisibility.PUBLIC
        )
        coEvery { userRepository.findById(testUserId) } returns adminUser
        coEvery { projectRepository.existsByKey(request.key) } returns false
        coEvery { projectRepository.create(any()) } answers { firstArg() }

        val result = projectService.createProject(testUserId, request)

        assertEquals(request.key, result.key)
    }

    @Test
    fun `createProject - success as SUPER_ADMIN`() = runBlocking {
        val superAdmin = testUser.copy(role = UserRole.SUPER_ADMIN)
        val request = ProjectCreate(
            type = ProjectType.TEAM_MANAGED,
            name = "Super Project",
            key = "SP",
            description = null,
            priority = ProjectPriority.MEDIUM,
            visibility = ProjectVisibility.PUBLIC
        )
        coEvery { userRepository.findById(testUserId) } returns superAdmin
        coEvery { projectRepository.existsByKey(request.key) } returns false
        coEvery { projectRepository.create(any()) } answers { firstArg() }

        val result = projectService.createProject(testUserId, request)

        assertEquals(request.key, result.key)
    }

    @Test
    fun `createProject - fails when user is not ADMIN or SUPER_ADMIN`() = runBlocking {
        val regularUser = testUser.copy(role = UserRole.USER)
        val request = mockk<ProjectCreate>()
        coEvery { userRepository.findById(testUserId) } returns regularUser

        val ex = assertFailsWith<DomainException.AccessDenied> {
            projectService.createProject(testUserId, request)
        }
        assertEquals("Insufficient permissions", ex.message)
        coVerify(exactly = 0) { projectRepository.create(any()) }
    }

    @Test
    fun `createProject - fails when user not found`() = runBlocking {
        val request = mockk<ProjectCreate>()
        coEvery { userRepository.findById(testUserId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            projectService.createProject(testUserId, request)
        }
        assertEquals("User with id 'ID: $testUserId' not found", ex.message)
    }

    @Test
    fun `createProject - fails when project key already exists`() = runBlocking {
        val adminUser = testUser.copy(role = UserRole.ADMIN)
        val request = ProjectCreate(
            type = ProjectType.TEAM_MANAGED,
            name = "new Project",
            key = "EXIST",
            description = null,
            priority = ProjectPriority.MEDIUM,
            visibility = ProjectVisibility.PUBLIC
        )
        coEvery { userRepository.findById(testUserId) } returns adminUser
        coEvery { projectRepository.existsByKey("EXIST") } returns true

        val ex = assertFailsWith<DomainException.AlreadyExists> {
            projectService.createProject(testUserId, request)
        }
        assertEquals("Project with key EXIST already exists", ex.message)
    }

    // ==================== updateProject ====================

    @Test
    fun `updateProject - success`() = runBlocking {
        val request = ProjectUpdate(
            name = "Updated Name",
            description = "New Desc",
            status = ProjectStatus.ON_HOLD,
            priority = ProjectPriority.LOW,
            visibility = ProjectVisibility.PRIVATE
        )
        coEvery { userRepository.findById(testUserId) } returns testUser
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns testMember
        coEvery { projectRepository.existsByName(request.name!!) } returns false
        coEvery { projectRepository.update(any()) } answers { firstArg() }

        val result = projectService.updateProject(testUserId, testProjectId, request)

        assertEquals(request.name, result.name)
        assertEquals(request.description, result.description)
        assertEquals(request.status, result.status)
    }

    @Test
    fun `updateProject - fails when project not found`() = runBlocking {
        val request = mockk<ProjectUpdate>()
        coEvery { userRepository.findById(testUserId) } returns testUser
        coEvery { projectRepository.findById(testProjectId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            projectService.updateProject(testUserId, testProjectId, request)
        }
        assertEquals("Project with id 'Project Not Found' not found", ex.message)
    }

    @Test
    fun `updateProject - fails when user not a member`() = runBlocking {
        val request = mockk<ProjectUpdate>()
        coEvery { userRepository.findById(testUserId) } returns testUser
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns null

        val ex = assertFailsWith<DomainException.AccessDenied> {
            projectService.updateProject(testUserId, testProjectId, request)
        }
        assertEquals("User is not a member of the project", ex.message)
    }

    @Test
    fun `updateProject - fails when name already exists`() = runBlocking {
        val request = ProjectUpdate(name = "Existing Name")
        coEvery { userRepository.findById(testUserId) } returns testUser
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns testMember
        coEvery { projectRepository.existsByName("Existing Name") } returns true

        val ex = assertFailsWith<DomainException.AlreadyExists> {
            projectService.updateProject(testUserId, testProjectId, request)
        }
        assertEquals("Project with name ${request.name} already exists", ex.message)
    }

    // ==================== deleteProject ====================

    @Test
    fun `deleteProject - success as OWNER`() = runBlocking {
        coEvery { userRepository.findById(testUserId) } returns testUser
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns testMember
        coEvery { projectRepository.delete(testProjectId) } returns true

        projectService.deleteProject(testUserId, testProjectId)

        coVerify { projectRepository.delete(testProjectId) }
    }

    @Test
    fun `deleteProject - success as SUPER_ADMIN without membership`() = runBlocking {
        val adminUser = testUser.copy(role = UserRole.SUPER_ADMIN)
        coEvery { userRepository.findById(testUserId) } returns adminUser
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectRepository.delete(testProjectId) } returns true

        projectService.deleteProject(testUserId, testProjectId)

        coVerify(exactly = 0) { projectMemberRepository.findByProjectAndUser(any(), any()) }
        coVerify { projectRepository.delete(testProjectId) }
    }

    @Test
    fun `deleteProject - success as ADMIN without membership`() = runBlocking {
        val adminUser = testUser.copy(role = UserRole.ADMIN)
        coEvery { userRepository.findById(testUserId) } returns adminUser
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectRepository.delete(testProjectId) } returns true

        projectService.deleteProject(testUserId, testProjectId)

        coVerify(exactly = 0) { projectMemberRepository.findByProjectAndUser(any(), any()) }
        coVerify { projectRepository.delete(testProjectId) }
    }

    @Test
    fun `deleteProject - fails when not owner and not admin`() = runBlocking {
        val normalUser = testUser.copy(role = UserRole.USER)
        val member = testMember.copy(role = ProjectRole.MEMBER)
        coEvery { userRepository.findById(testUserId) } returns normalUser
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member

        val ex = assertFailsWith<DomainException.AccessDenied> {
            projectService.deleteProject(testUserId, testProjectId)
        }
        assertEquals("Insufficient permissions: MEMBER < OWNER", ex.message)
    }

    // ==================== findById, findByName, findByKey ====================

    @Test
    fun `findById - success`() = runBlocking {
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        val result = projectService.findById(testProjectId)
        assertEquals(testProject, result)
    }

    @Test
    fun `findById - not found`() = runBlocking {
        coEvery { projectRepository.findById(testProjectId) } returns null
        val ex = assertFailsWith<DomainException.NotFound> {
            projectService.findById(testProjectId)
        }
        assertEquals("Project with id '$testProjectId' not found", ex.message)
    }

    @Test
    fun `findByName - success`() = runBlocking {
        coEvery { projectRepository.findByName("Test Project") } returns testProject
        val result = projectService.findByName("Test Project")
        assertEquals(testProject, result)
    }

    @Test
    fun `findByName - not found`() = runBlocking {
        coEvery { projectRepository.findByName("Unknown") } returns null
        val ex = assertFailsWith<DomainException.NotFound> {
            projectService.findByName("Unknown")
        }
        assertEquals("Project with id 'Unknown' not found", ex.message)
    }

    @Test
    fun `findByKey - success`() = runBlocking {
        coEvery { projectRepository.findByKey("TP") } returns testProject
        val result = projectService.findByKey("TP")
        assertEquals(testProject, result)
    }

    @Test
    fun `findByKey - not found`() = runBlocking {
        coEvery { projectRepository.findByKey("XXX") } returns null
        val ex = assertFailsWith<DomainException.NotFound> {
            projectService.findByKey("XXX")
        }
        assertEquals("Project with id 'XXX' not found", ex.message)
    }

    // ==================== findByUser ====================

    @Test
    fun `findByUser - success`() = runBlocking {
        val projects = listOf(testProject)
        coEvery { userRepository.findById(testUserId) } returns testUser
        coEvery { projectRepository.findByUser(testUserId, 10, 0) } returns projects

        val result = projectService.findByUser(testUserId, 10, 0)
        assertEquals(projects, result)
    }

    @Test
    fun `findByUser - user not found`() = runBlocking {
        coEvery { userRepository.findById(testUserId) } returns null
        val ex = assertFailsWith<DomainException.NotFound> {
            projectService.findByUser(testUserId, 10, 0)
        }
        assertEquals("User with id 'User Not Found' not found", ex.message)
    }

    // ==================== updateStatus ====================

    @Test
    fun `updateStatus - success to ARCHIVED`() = runBlocking {
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns testMember
        coEvery { projectRepository.updateStatus(testProjectId, ProjectStatus.ARCHIVED) } returns true

        val result = projectService.updateStatus(testUserId, testProjectId, ProjectStatus.ARCHIVED)
        assertTrue(result)
    }

    // ==================== getProjectMembers ====================

    @Test
    fun `getProjectMembers - success`() = runBlocking {
        val members = listOf(testMember)
        coEvery { projectMemberRepository.findByProject(testProjectId, 10, 0) } returns members

        val result = projectService.getProjectMembers(testProjectId, testUserId, 1, 10)
        assertEquals(1, result.size)
        assertEquals(testMember.userId.toString(), result.first().userId)
    }

    // ==================== updateMemberRole ====================

    @Test
    fun `updateMemberRole - success by OWNER`() = runBlocking {
        val targetUserId = UUID.randomUUID()
        val targetMember = testMember.copy(id = UUID.randomUUID(), userId = targetUserId, role = ProjectRole.MEMBER)
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns testMember // OWNER
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, targetUserId) } returns targetMember
        coEvery { projectMemberRepository.updateRole(targetMember.id, ProjectRole.ADMIN) } returns Unit
        coEvery { projectMemberRepository.findByProjectAndRole(testProjectId, ProjectRole.OWNER) } returns listOf(testMember)

        projectService.updateMemberRole(testProjectId, testUserId, targetUserId, ProjectRole.ADMIN)

        coVerify { projectMemberRepository.updateRole(targetMember.id, ProjectRole.ADMIN) }
    }

    @Test
    fun `updateMemberRole - cannot change own role`() = runBlocking {
        val ex = assertFailsWith<DomainException.Forbidden> {
            projectService.updateMemberRole(testProjectId, testUserId, testUserId, ProjectRole.ADMIN)
        }
        assertEquals("Not allowed", ex.message)
    }

    @Test
    fun `updateMemberRole - manager cannot grant ADMIN`() = runBlocking {
        val managerMember = testMember.copy(role = ProjectRole.MANAGER)
        val targetUserId = UUID.randomUUID()
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns managerMember
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, targetUserId) } returns mockk()

        val ex = assertFailsWith<DomainException.AccessDenied> {
            projectService.updateMemberRole(testProjectId, testUserId, targetUserId, ProjectRole.ADMIN)
        }
        assertEquals("Manager cannot grant ADMIN or OWNER roles", ex.message)
    }

    @Test
    fun `updateMemberRole - last OWNER cannot be demoted`() = runBlocking {
        val targetUserId = UUID.randomUUID()
        val targetOwner = testMember.copy(
            id = UUID.randomUUID(),
            userId = targetUserId,
            role = ProjectRole.OWNER
        )
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns testMember // текущий OWNER
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, targetUserId) } returns targetOwner
        coEvery { projectMemberRepository.findByProjectAndRole(testProjectId, ProjectRole.OWNER) } returns listOf(targetOwner) // только один OWNER

        val ex = assertFailsWith<DomainException.BusinessRule> {
            projectService.updateMemberRole(testProjectId, testUserId, targetUserId, ProjectRole.MEMBER)
        }
        assertEquals("Project must have at least one OWNER", ex.message)
    }

    // ==================== removeMember ====================

    @Test
    fun `removeMember - success by OWNER`() = runBlocking {
        val targetUserId = UUID.randomUUID()
        val targetMember = testMember.copy(id = UUID.randomUUID(), userId = targetUserId, role = ProjectRole.MEMBER)
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns testMember // OWNER
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, targetUserId) } returns targetMember
        coEvery { projectMemberRepository.remove(targetMember.id) } returns Unit

        projectService.removeMember(testProjectId, testUserId, targetUserId)

        coVerify { projectMemberRepository.remove(targetMember.id) }
    }

    @Test
    fun `removeMember - manager cannot remove themselves`() = runBlocking {
        val managerMember = testMember.copy(role = ProjectRole.MANAGER, userId = testUserId)
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns managerMember

        val ex = assertFailsWith<DomainException.Forbidden> {
            projectService.removeMember(testProjectId, testUserId, testUserId)
        }
        assertEquals("Manager cannot remove themselves", ex.message)
    }

    @Test
    fun `removeMember - only OWNER can remove another OWNER`() = runBlocking {
        val targetOwner = testMember.copy(role = ProjectRole.OWNER, userId = UUID.randomUUID())
        val managerMember = testMember.copy(role = ProjectRole.MANAGER)
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns managerMember
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, targetOwner.userId) } returns targetOwner

        val ex = assertFailsWith<DomainException.AccessDenied> {
            projectService.removeMember(testProjectId, testUserId, targetOwner.userId)
        }
        assertEquals("Only OWNER can remove another OWNER", ex.message)
    }

    // ==================== leaveProject ====================

    @Test
    fun `leaveProject - success as MEMBER`() = runBlocking {
        val member = testMember.copy(role = ProjectRole.MEMBER)
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { projectMemberRepository.remove(member.id) } returns Unit

        projectService.leaveProject(testProjectId, testUserId)

        coVerify { projectMemberRepository.remove(member.id) }
    }

    @Test
    fun `leaveProject - last OWNER cannot leave`() = runBlocking {
        val owner = testMember.copy(role = ProjectRole.OWNER)
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns owner
        coEvery { projectMemberRepository.findByProjectAndRole(testProjectId, ProjectRole.OWNER) } returns listOf(owner)

        val ex = assertFailsWith<DomainException.BusinessRule> {
            projectService.leaveProject(testProjectId, testUserId)
        }
        assertEquals("Cannot leave project: you are the last OWNER. Please assign another OWNER first.", ex.message)
    }

    @Test
    fun `leaveProject - user not a member`() = runBlocking {
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            projectService.leaveProject(testProjectId, testUserId)
        }
        assertEquals("User is not a member of the project with id '$testUserId' not found", ex.message)
    }
}