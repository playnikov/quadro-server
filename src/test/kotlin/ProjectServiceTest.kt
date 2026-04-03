package com.quadro

import com.quadro.datasource.repositories.company.CompanyMemberRepository
import com.quadro.datasource.repositories.company.CompanyRepository
import com.quadro.datasource.repositories.project.ProjectMemberRepository
import com.quadro.datasource.repositories.project.ProjectRepository
import com.quadro.datasource.repositories.project.ProjectTeamRepository
import com.quadro.datasource.repositories.team.TeamMemberRepository
import com.quadro.datasource.repositories.team.TeamRepository
import com.quadro.datasource.repositories.users.UserRepository
import com.quadro.domain.models.company.Company
import com.quadro.domain.models.company.CompanyMember
import com.quadro.domain.models.company.CompanyRole
import com.quadro.domain.models.company.CompanyStatus
import com.quadro.domain.models.project.AddProjectMembers
import com.quadro.domain.models.project.AssignTeam
import com.quadro.domain.models.project.Project
import com.quadro.domain.models.project.ProjectCreate
import com.quadro.domain.models.project.ProjectMember
import com.quadro.domain.models.project.ProjectPriority
import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.project.ProjectSettings
import com.quadro.domain.models.project.ProjectStatus
import com.quadro.domain.models.project.ProjectTeam
import com.quadro.domain.models.project.ProjectType
import com.quadro.domain.models.project.ProjectUpdate
import com.quadro.domain.models.project.ProjectVisibility
import com.quadro.domain.models.team.Team
import com.quadro.domain.models.team.TeamMember
import com.quadro.domain.models.team.TeamRole
import com.quadro.domain.models.team.TeamSettings
import com.quadro.domain.models.team.TeamStatus
import com.quadro.domain.models.team.TeamVisibility
import com.quadro.domain.models.user.DomainUserRole
import com.quadro.domain.models.user.User
import com.quadro.domain.services.project.ProjectServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectServiceTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var projectTeamRepository: ProjectTeamRepository
    private lateinit var projectMemberRepository: ProjectMemberRepository
    private lateinit var companyRepository: CompanyRepository
    private lateinit var companyMemberRepository: CompanyMemberRepository
    private lateinit var teamRepository: TeamRepository
    private lateinit var teamMemberRepository: TeamMemberRepository
    private lateinit var userRepository: UserRepository
    private lateinit var projectService: ProjectServiceImpl

    private val testCompanyId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testProjectId = UUID.randomUUID()
    private val testTeamId = UUID.randomUUID()
    private val testTargetUserId = UUID.randomUUID()
    private val testMemberId = UUID.randomUUID()
    private val testProjectTeamId = UUID.randomUUID()

    @Before
    fun setup() {
        projectRepository = mockk(relaxed = true)
        projectTeamRepository = mockk(relaxed = true)
        projectMemberRepository = mockk(relaxed = true)
        companyRepository = mockk(relaxed = true)
        companyMemberRepository = mockk(relaxed = true)
        teamRepository = mockk(relaxed = true)
        teamMemberRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)

        projectService = ProjectServiceImpl(
            projectRepository = projectRepository,
            projectTeamRepository = projectTeamRepository,
            projectMemberRepository = projectMemberRepository,
            companyRepository = companyRepository,
            companyMemberRepository = companyMemberRepository,
            teamRepository = teamRepository,
            teamMemberRepository = teamMemberRepository,
            userRepository = userRepository
        )
    }

    @Test
    fun `createProject - should create project successfully`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val companyMember = createTestCompanyMember(role = CompanyRole.ADMIN)
        val request = ProjectCreate(
            companyId = testCompanyId,
            name = "Test Project",
            key = "TEST",
            description = "Test Description",
            priority = ProjectPriority.HIGH,
            visibility = ProjectVisibility.RESTRICTED,
            leadId = testUserId,
            type = ProjectType.TEAM_MANAGED
        )

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns companyMember
        coEvery { projectRepository.existsByKey(testCompanyId, "TEST") } returns false
        coEvery { projectRepository.existsByName(testCompanyId, "Test Project") } returns false
        coEvery { projectRepository.create(any()) } answers { firstArg() }
        coEvery { projectMemberRepository.add(any()) } returns mockk()

        // Act
        val result = projectService.createProject(testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        val project = result.getOrNull()
        assertEquals("Test Project", project?.name)
        assertEquals("TEST", project?.key)
        assertEquals(testUserId, project?.ownerId)

        coVerify(exactly = 1) { projectMemberRepository.add(match { member ->
            member.role == ProjectRole.LEAD && member.userId == testUserId
        }) }

        coVerify(exactly = 1) { projectMemberRepository.add(match { member ->
            member.role == ProjectRole.LEAD && member.userId == testUserId
        }) }
    }

    @Test
    fun `createProject - should fail when company not found`() = runBlocking {
        // Arrange
        val request = ProjectCreate(
            companyId = testCompanyId,
            name = "Test Project",
            key = "TEST",
            leadId = testUserId
        )

        coEvery { companyRepository.findById(testCompanyId) } returns null

        // Act
        val result = projectService.createProject(testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Company not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createProject - should fail when user not in company`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val request = ProjectCreate(
            companyId = testCompanyId,
            name = "Test Project",
            key = "TEST",
            leadId = testUserId
        )

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns null

        // Act
        val result = projectService.createProject(testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User is not a member of this company", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProject - should fail when project not found`() = runBlocking {
        // Arrange
        coEvery { projectRepository.findById(testProjectId) } returns null

        // Act
        val result = projectService.getProject(testProjectId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Project not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProject - should return project when user has access`() = runBlocking {
        // Arrange
        val project = createTestProject()

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true

        // Act
        val result = projectService.getProject(testProjectId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(testProjectId, result.getOrNull()?.id)
    }

    @Test
    fun `updateProject - should update project when user has permission`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.ADMIN)
        val request = ProjectUpdate(
            name = "Updated Name",
            description = "Updated Description"
        )

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { projectRepository.existsByName(testCompanyId, "Updated Name") } returns false
        coEvery { projectRepository.update(any()) } answers { firstArg() }

        // Act
        val result = projectService.updateProject(testProjectId, testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Updated Name", result.getOrNull()?.name)
        assertEquals("Updated Description", result.getOrNull()?.description)

        coVerify(exactly = 1) { projectRepository.update(any()) }
    }

    @Test
    fun `updateProject - should fail with insufficient permissions`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.MEMBER)
        val request = ProjectUpdate(name = "Updated Name")

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member

        // Act
        val result = projectService.updateProject(testProjectId, testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Insufficient permissions", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteProject - should delete project when user is owner`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.OWNER)

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { projectRepository.delete(testProjectId) } returns true

        // Act
        val result = projectService.deleteProject(testProjectId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { projectTeamRepository.removeAllByProject(testProjectId) }
        coVerify(exactly = 1) { projectMemberRepository.removeAllByProject(testProjectId) }
        coVerify(exactly = 1) { projectRepository.delete(testProjectId) }
    }

    @Test
    fun `deleteProject - should fail when user is not owner`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.ADMIN)

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member

        // Act
        val result = projectService.deleteProject(testProjectId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Only project owner can delete the project", result.exceptionOrNull()?.message)
    }
    @Test
    fun `archiveProject - should archive project`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.ADMIN)

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { projectRepository.updateStatus(testProjectId, ProjectStatus.ARCHIVED) } returns true

        // Act
        val result = projectService.archiveProject(testProjectId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { projectRepository.updateStatus(testProjectId, ProjectStatus.ARCHIVED) }
    }

    @Test
    fun `restoreProject - should restore archived project`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.ADMIN)

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { projectRepository.updateStatus(testProjectId, ProjectStatus.ACTIVE) } returns true

        // Act
        val result = projectService.restoreProject(testProjectId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { projectRepository.updateStatus(testProjectId, ProjectStatus.ACTIVE) }
    }

    @Test
    fun `getCompanyProjects - should return company projects`() = runBlocking {
        // Arrange
        val projects = listOf(
            createTestProject(id = UUID.randomUUID()),
            createTestProject(id = UUID.randomUUID())
        )

        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns true
        coEvery { projectRepository.findByCompany(testCompanyId, 10, 0) } returns projects

        // Act
        val result = projectService.getCompanyProjects(testCompanyId, testUserId, 1, 10)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getCompanyProjects - should fail when user not in company`() = runBlocking {
        // Arrange
        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns false

        // Act
        val result = projectService.getCompanyProjects(testCompanyId, testUserId, 1, 10)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User is not a member of this company", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserProjects - should return user projects`() = runBlocking {
        // Arrange
        val projects = listOf(
            createTestProject(id = UUID.randomUUID()),
            createTestProject(id = UUID.randomUUID())
        )

        coEvery { projectRepository.findByUser(testUserId, null) } returns projects

        // Act
        val result = projectService.getUserProjects(testUserId, null)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getUserProjects - should filter by company`() = runBlocking {
        // Arrange
        coEvery { projectRepository.findByUser(testUserId, testCompanyId) } returns emptyList()

        // Act
        projectService.getUserProjects(testUserId, testCompanyId)

        // Assert
        coVerify(exactly = 1) { projectRepository.findByUser(testUserId, testCompanyId) }
    }

    @Test
    fun `getTeamProjects - should return team projects`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val projects = listOf(
            createTestProject(id = UUID.randomUUID()),
            createTestProject(id = UUID.randomUUID())
        )

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.exists(testTeamId, testUserId) } returns true
        coEvery { projectRepository.findByTeam(testTeamId) } returns projects

        // Act
        val result = projectService.getTeamProjects(testTeamId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getTeamProjects - should fail when user not in team`() = runBlocking {
        // Arrange
        val team = createTestTeam()

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.exists(testTeamId, testUserId) } returns false

        // Act
        val result = projectService.getTeamProjects(testTeamId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User is not a member of this team", result.exceptionOrNull()?.message)
    }

    @Test
    fun `searchProjects - should return search results`() = runBlocking {
        // Arrange
        val projects = listOf(
            createTestProject(id = UUID.randomUUID(), name = "Backend API"),
            createTestProject(id = UUID.randomUUID(), name = "Backend DB")
        )

        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns true
        coEvery { projectRepository.search(testCompanyId, "backend", 20) } returns projects

        // Act
        val result = projectService.searchProjects(testCompanyId, testUserId, "backend")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `searchProjects - should fail when user not in company`() = runBlocking {
        // Arrange
        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns false

        // Act
        val result = projectService.searchProjects(testCompanyId, testUserId, "test")

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User is not a member of this company", result.exceptionOrNull()?.message)
    }

    @Test
    fun `assignTeam - should assign team to project`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val team = createTestTeam()
        val member = createTestProjectMember(role = ProjectRole.ADMIN)
        val request = AssignTeam(
            teamId = testTeamId,
            role = ProjectRole.MEMBER,
            isLeadTeam = false
        )

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { projectTeamRepository.exists(testProjectId, testTeamId) } returns false
        coEvery { projectTeamRepository.assign(any()) } answers { firstArg() }

        // Act
        val result = projectService.assignTeam(testProjectId, testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `assignTeam - should fail when team already assigned`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val team = createTestTeam()
        val member = createTestProjectMember(role = ProjectRole.ADMIN)
        val request = AssignTeam(teamId = testTeamId)

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { projectTeamRepository.exists(testProjectId, testTeamId) } returns true // Уже назначена

        // Act
        val result = projectService.assignTeam(testProjectId, testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Team already assigned to this project", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getAssignedTeams - should return assigned teams`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val teams = listOf(
            createTestProjectTeam(id = UUID.randomUUID()),
            createTestProjectTeam(id = UUID.randomUUID())
        )

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true
        coEvery { projectTeamRepository.findByProject(testProjectId) } returns teams

        // Act
        val result = projectService.getAssignedTeams(testProjectId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getAssignedTeams - should fail when access denied`() = runBlocking {
        // Arrange
        val project = createTestProject()

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns false
        coEvery { teamMemberRepository.findByUser(testUserId, testCompanyId) } returns emptyList()

        // Act
        val result = projectService.getAssignedTeams(testProjectId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Access denied", result.exceptionOrNull()?.message)
    }

    @Test
    fun `unassignTeam - should unassign team`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val team = createTestTeam()
        val member = createTestProjectMember(role = ProjectRole.ADMIN)

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { projectTeamRepository.removeByProjectAndTeam(testProjectId, testTeamId) } returns true
        coEvery { projectMemberRepository.removeAllByTeam(testTeamId, testProjectId) } returns 3

        // Act
        val result = projectService.unassignTeam(testProjectId, testUserId, testTeamId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { projectTeamRepository.removeByProjectAndTeam(testProjectId, testTeamId) }
        coVerify(exactly = 1) { projectMemberRepository.removeAllByTeam(testTeamId, testProjectId) }
    }

    @Test
    fun `unassignTeam - should fail with insufficient permissions`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.MEMBER) // MEMBER не может управлять командами

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member

        // Act
        val result = projectService.unassignTeam(testProjectId, testUserId, testTeamId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Insufficient permissions", result.exceptionOrNull()?.message)
    }

    @Test
    fun `addMembers - should add members to project`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val currentUser = createTestProjectMember(role = ProjectRole.ADMIN)
        val request = AddProjectMembers(
            userIds = listOf(UUID.randomUUID(), UUID.randomUUID()),
            role = ProjectRole.MEMBER
        )

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns currentUser
        coEvery { companyMemberRepository.exists(project.companyId, any()) } returns true
        coEvery { projectMemberRepository.exists(testProjectId, any()) } returns false
        coEvery { projectMemberRepository.addAll(any()) } answers { firstArg() }

        // Act
        val result = projectService.addMembers(testProjectId, testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        coVerify(exactly = 1) { projectMemberRepository.addAll(any()) }
    }

    @Test
    fun `addMembers - should fail when user not in company`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val currentUser = createTestProjectMember(role = ProjectRole.ADMIN)
        val targetUserId = UUID.randomUUID()
        val request = AddProjectMembers(
            userIds = listOf(targetUserId),
            role = ProjectRole.MEMBER
        )

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns currentUser
        coEvery { companyMemberRepository.exists(project.companyId, targetUserId) } returns false

        // Act
        val result = projectService.addMembers(testProjectId, testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User $targetUserId is not a member of the company", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProjectMembers - should return project members`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val members = listOf(
            createTestProjectMember(id = UUID.randomUUID(), userId = UUID.randomUUID()),
            createTestProjectMember(id = UUID.randomUUID(), userId = UUID.randomUUID())
        )

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true
        coEvery { projectMemberRepository.findByProject(testProjectId, 20, 0) } returns members

        // Act
        val result = projectService.getProjectMembers(testProjectId, testUserId, 1, 20)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getProjectMembers - should fail when access denied`() = runBlocking {
        // Arrange
        val project = createTestProject()

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns false
        coEvery { teamMemberRepository.findByUser(testUserId, testCompanyId) } returns emptyList()

        // Act
        val result = projectService.getProjectMembers(testProjectId, testUserId, 1, 20)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Access denied", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProjectMember - should return specific member`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(id = testMemberId, userId = testTargetUserId)

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testTargetUserId) } returns member

        // Act
        val result = projectService.getProjectMember(testProjectId, testUserId, testTargetUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(testTargetUserId, result.getOrNull()?.userId)
    }

    @Test
    fun `getProjectMember - should fail when member not found`() = runBlocking {
        // Arrange
        val project = createTestProject()

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testTargetUserId) } returns null

        // Act
        val result = projectService.getProjectMember(testProjectId, testUserId, testTargetUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User is not a member of this project", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateMemberRole - should update member role`() = runBlocking {
        // Arrange
        val currentUser = createTestProjectMember(id = UUID.randomUUID(), role = ProjectRole.ADMIN)
        val targetUser = createTestProjectMember(id = testMemberId, role = ProjectRole.MEMBER)

        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns currentUser
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testTargetUserId) } returns targetUser
        coEvery { projectMemberRepository.updateRole(testMemberId, ProjectRole.ADMIN) } returns true

        // Act
        val result = projectService.updateMemberRole(testProjectId, testUserId, testTargetUserId, ProjectRole.ADMIN)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { projectMemberRepository.updateRole(testMemberId, ProjectRole.ADMIN) }
    }

    @Test
    fun `updateMemberRole - should fail when target is from team`() = runBlocking {
        // Arrange
        val currentUser = createTestProjectMember(role = ProjectRole.ADMIN)
        val targetUser = createTestProjectMember(
            id = testMemberId,
            role = ProjectRole.MEMBER,
            sourceTeamId = testTeamId
        )

        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns currentUser
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testTargetUserId) } returns targetUser

        // Act
        val result = projectService.updateMemberRole(testProjectId, testUserId, testTargetUserId, ProjectRole.ADMIN)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Member from team must be managed through team settings", result.exceptionOrNull()?.message)
    }

    @Test
    fun `removeMember - should remove member`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val currentUser = createTestProjectMember(role = ProjectRole.OWNER)
        val targetUser = createTestProjectMember(id = testMemberId, role = ProjectRole.MEMBER)

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns currentUser
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testTargetUserId) } returns targetUser
        coEvery { projectMemberRepository.remove(testMemberId) } returns true

        // Act
        val result = projectService.removeMember(testProjectId, testUserId, testTargetUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { projectMemberRepository.remove(testMemberId) }
    }

    @Test
    fun `removeMember - should fail when target is owner`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val currentUser = createTestProjectMember(role = ProjectRole.ADMIN)
        val targetUser = createTestProjectMember(role = ProjectRole.OWNER)

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns currentUser
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testTargetUserId) } returns targetUser

        // Act
        val result = projectService.removeMember(testProjectId, testUserId, testTargetUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Cannot remove project owner", result.exceptionOrNull()?.message)
    }

    @Test
    fun `leaveProject - should allow member to leave`() = runBlocking {
        // Arrange
        val member = createTestProjectMember(id = testMemberId, role = ProjectRole.MEMBER)

        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { projectMemberRepository.remove(testMemberId) } returns true

        // Act
        val result = projectService.leaveProject(testProjectId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { projectMemberRepository.remove(testMemberId) }
    }

    @Test
    fun `leaveProject - should fail when owner tries to leave`() = runBlocking {
        // Arrange
        val member = createTestProjectMember(role = ProjectRole.OWNER)

        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member

        // Act
        val result = projectService.leaveProject(testProjectId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Owner cannot leave the project. Transfer ownership first.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getProjectPermissions - should return permissions for member`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.ADMIN)

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member

        // Act
        val result = projectService.getProjectPermissions(testProjectId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        val permissions = result.getOrNull()
        assertTrue(permissions?.canEdit == true)
        assertTrue(permissions?.canManageTeams == true)
    }

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
        companySettings = com.quadro.domain.models.company.CompanySettings(),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        deletedAt = null
    )

    private fun createTestCompanyMember(role: CompanyRole): CompanyMember = CompanyMember(
        id = UUID.randomUUID(),
        companyId = testCompanyId,
        userId = testUserId,
        role = role,
        joinedAt = System.currentTimeMillis(),
        invitedBy = testUserId,
        invitedAt = System.currentTimeMillis(),
        isActive = true
    )

    private fun createTestProject(
        id: UUID = testProjectId,
        name: String = "Test Project",
        visibility: ProjectVisibility = ProjectVisibility.RESTRICTED
    ): Project = Project(
        id = id,
        companyId = testCompanyId,
        type = ProjectType.TEAM_MANAGED,
        name = name,
        key = "TEST",
        description = null,
        status = ProjectStatus.ACTIVE,
        priority = ProjectPriority.MEDIUM,
        visibility = visibility,
        leadId = testUserId,
        ownerId = testUserId,
        settings = ProjectSettings(),
        startDate = null,
        endDate = null,
        completedAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        archivedAt = null
    )

    private fun createTestProjectMember(
        id: UUID = testMemberId,
        userId: UUID = testUserId,
        role: ProjectRole = ProjectRole.MEMBER,
        sourceTeamId: UUID? = null
    ): ProjectMember = ProjectMember(
        id = id,
        projectId = testProjectId,
        userId = userId,
        role = role,
        joinedAt = System.currentTimeMillis(),
        invitedBy = testUserId,
        invitedAt = System.currentTimeMillis(),
        sourceTeamId = sourceTeamId
    )

    private fun createTestProjectTeam(
        id: UUID = testProjectTeamId
    ): ProjectTeam = ProjectTeam(
        id = id,
        projectId = testProjectId,
        teamId = testTeamId,
        role = ProjectRole.MEMBER,
        isLeadTeam = false,
        assignedAt = System.currentTimeMillis(),
        assignedBy = testUserId
    )

    private fun createTestTeam(): Team = Team(
        id = testTeamId,
        companyId = testCompanyId,
        name = "Test Team",
        description = null,
        avatar = null,
        status = TeamStatus.ACTIVE,
        visibility = TeamVisibility.PRIVATE,
        leadId = testUserId,
        settings = TeamSettings(),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        archivedAt = null,
        currentMembers = 5
    )

    private fun createTestTeamMember(userId: UUID = testUserId): TeamMember = TeamMember(
        id = UUID.randomUUID(),
        teamId = testTeamId,
        userId = userId,
        role = TeamRole.MEMBER,
        joinedAt = System.currentTimeMillis(),
        invitedBy = testUserId,
        invitedAt = System.currentTimeMillis(),
        isActive = true
    )

    private fun createTestUser(): User = User(
        id = testUserId,
        email = "test@example.com",
        username = "testuser",
        passwordHash = "hash",
        firstName = "Test",
        lastName = "User",
        role = DomainUserRole.USER,
        isEmailVerified = true,
        isActive = true,
        avatar = null
    )
}