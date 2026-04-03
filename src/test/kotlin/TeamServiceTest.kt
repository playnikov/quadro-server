package com.quadro

import com.quadro.datasource.repositories.company.CompanyMemberRepository
import com.quadro.datasource.repositories.company.CompanyRepository
import com.quadro.datasource.repositories.team.TeamMemberRepository
import com.quadro.datasource.repositories.team.TeamRepository
import com.quadro.datasource.repositories.users.UserRepository
import com.quadro.domain.models.company.Company
import com.quadro.domain.models.company.CompanyMember
import com.quadro.domain.models.company.CompanyRole
import com.quadro.domain.models.company.CompanySettings
import com.quadro.domain.models.company.CompanyStatus
import com.quadro.domain.models.team.AddTeamMembersRequest
import com.quadro.domain.models.team.Team
import com.quadro.domain.models.team.TeamCreate
import com.quadro.domain.models.team.TeamMember
import com.quadro.domain.models.team.TeamMemberStats
import com.quadro.domain.models.team.TeamRole
import com.quadro.domain.models.team.TeamSettings
import com.quadro.domain.models.team.TeamStatus
import com.quadro.domain.models.team.TeamUpdate
import com.quadro.domain.models.team.TeamVisibility
import com.quadro.domain.models.user.DomainUserRole
import com.quadro.domain.models.user.User
import com.quadro.domain.services.team.TeamServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class TeamServiceTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var teamMemberRepository: TeamMemberRepository
    private lateinit var companyRepository: CompanyRepository
    private lateinit var companyMemberRepository: CompanyMemberRepository
    private lateinit var userRepository: UserRepository
    private lateinit var teamService: TeamServiceImpl

    private val testCompanyId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testTeamId = UUID.randomUUID()
    private val testTargetUserId = UUID.randomUUID()
    private val testMemberId = UUID.randomUUID()

    @Before
    fun setup() {
        teamRepository = mockk(relaxed = true)
        teamMemberRepository = mockk(relaxed = true)
        companyRepository = mockk(relaxed = true)
        companyMemberRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)

        teamService = TeamServiceImpl(
            teamRepository = teamRepository,
            teamMemberRepository = teamMemberRepository,
            companyRepository = companyRepository,
            companyMemberRepository = companyMemberRepository,
            userRepository = userRepository
        )
    }

    // ============== Тесты createTeam ==============

    @Test
    fun `createTeam - should create team successfully`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val companyMember = createTestCompanyMember(role = CompanyRole.ADMIN)
        val request = TeamCreate(
            companyId = testCompanyId,
            name = "Test Team",
            description = "Test Description",
            visibility = TeamVisibility.PRIVATE,
            initialMembers = listOf(UUID.randomUUID(), UUID.randomUUID())
        )

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns companyMember
        coEvery { teamRepository.existsByName(testCompanyId, "Test Team") } returns false
        coEvery { teamRepository.create(any()) } answers { firstArg() }
        coEvery { teamMemberRepository.add(any()) } returns mockk()
        coEvery { teamMemberRepository.addAll(any()) } returns mockk()
        coEvery { companyMemberRepository.exists(any(), any()) } returns true

        // Act
        val result = teamService.createTeam(testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        val teamResult = result.getOrNull()
        assertEquals("Test Team", teamResult?.name)

        coVerify(exactly = 1) { teamRepository.create(any()) }
        coVerify(exactly = 1) { teamMemberRepository.add(any()) } // LEAD
        coVerify(exactly = 1) { teamMemberRepository.addAll(any()) } // Initial members
    }

    @Test
    fun `createTeam - should fail when company not found`() = runBlocking {
        // Arrange
        val request = TeamCreate(
            companyId = testCompanyId,
            name = "Test Team"
        )

        coEvery { companyRepository.findById(testCompanyId) } returns null

        // Act
        val result = teamService.createTeam(testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Company not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createTeam - should fail when user not in company`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val request = TeamCreate(
            companyId = testCompanyId,
            name = "Test Team"
        )

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns null

        // Act
        val result = teamService.createTeam(testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User is not a member of this company", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createTeam - should fail with insufficient permissions`() = runBlocking {
        // Arrange
        val company = createTestCompany()
        val companyMember = createTestCompanyMember(role = CompanyRole.MEMBER) // MEMBER не может создавать
        val request = TeamCreate(
            companyId = testCompanyId,
            name = "Test Team"
        )

        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { companyMemberRepository.findByCompanyAndUser(testCompanyId, testUserId) } returns companyMember

        // Act
        val result = teamService.createTeam(testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Insufficient permissions to create team", result.exceptionOrNull()?.message)
    }

    // ============== Тесты getTeam ==============

    @Test
    fun `getTeam - should return team when user has access`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val company = createTestCompany()
        val lead = createTestUser()

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.exists(testTeamId, testUserId) } returns true
        coEvery { companyRepository.findById(testCompanyId) } returns company
        coEvery { userRepository.findById(team.leadId) } returns lead
        coEvery { teamMemberRepository.countByTeam(testTeamId) } returns 5

        // Act
        val result = teamService.getTeam(testTeamId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(testTeamId.toString(), result.getOrNull()?.id)
    }

    @Test
    fun `getTeam - should fail when team not found`() = runBlocking {
        // Arrange
        coEvery { teamRepository.findById(testTeamId) } returns null

        // Act
        val result = teamService.getTeam(testTeamId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Team not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getTeam - should fail when user has no access`() = runBlocking {
        // Arrange
        val team = createTestTeam(visibility = TeamVisibility.PRIVATE)

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.exists(testTeamId, testUserId) } returns false

        // Act
        val result = teamService.getTeam(testTeamId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Access denied", result.exceptionOrNull()?.message)
    }

    // ============== Тесты updateTeam ==============

    @Test
    fun `updateTeam - should update team when user is admin`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val member = createTestTeamMember(role = TeamRole.ADMIN)
        val request = TeamUpdate(
            name = "Updated Team",
            description = "Updated Description",
            visibility = TeamVisibility.PUBLIC
        )

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns member
        coEvery { teamRepository.existsByName(testCompanyId, "Updated Team") } returns false
        coEvery { teamRepository.update(any()) } answers { firstArg() }

        // Act
        val result = teamService.updateTeam(testTeamId, testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Updated Team", result.getOrNull()?.name)

        coVerify(exactly = 1) { teamRepository.update(any()) }
    }

    @Test
    fun `updateTeam - should fail with insufficient permissions`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val member = createTestTeamMember(role = TeamRole.MEMBER) // MEMBER не может обновлять
        val request = TeamUpdate(name = "Updated Team")

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns member

        // Act
        val result = teamService.updateTeam(testTeamId, testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Insufficient permissions", result.exceptionOrNull()?.message)
    }

    // ============== Тесты deleteTeam ==============

    @Test
    fun `deleteTeam - should delete team when user is lead`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val member = createTestTeamMember(role = TeamRole.LEAD)

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns member
        coEvery { teamRepository.delete(testTeamId) } returns true

        // Act
        val result = teamService.deleteTeam(testTeamId, testUserId)

        // Assert
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) { teamMemberRepository.removeAllByTeam(testTeamId) }
        coVerify(exactly = 1) { teamRepository.delete(testTeamId) }
    }

    @Test
    fun `deleteTeam - should fail when user is not lead`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val member = createTestTeamMember(role = TeamRole.ADMIN) // ADMIN не может удалять

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns member

        // Act
        val result = teamService.deleteTeam(testTeamId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Only team lead can delete the team", result.exceptionOrNull()?.message)
    }

    // ============== Тесты archiveTeam / restoreTeam ==============

    @Test
    fun `archiveTeam - should archive team`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val member = createTestTeamMember(role = TeamRole.ADMIN)

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns member
        coEvery { teamRepository.updateStatus(testTeamId, TeamStatus.ARCHIVED) } returns true

        // Act
        val result = teamService.archiveTeam(testTeamId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { teamRepository.updateStatus(testTeamId, TeamStatus.ARCHIVED) }
    }

    @Test
    fun `restoreTeam - should restore team`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val member = createTestTeamMember(role = TeamRole.ADMIN)

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns member
        coEvery { teamRepository.updateStatus(testTeamId, TeamStatus.ACTIVE) } returns true

        // Act
        val result = teamService.restoreTeam(testTeamId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { teamRepository.updateStatus(testTeamId, TeamStatus.ACTIVE) }
    }

    // ============== Тесты getCompanyTeams ==============

    @Test
    fun `getCompanyTeams - should return company teams`() = runBlocking {
        // Arrange
        val teams = listOf(
            createTestTeam(id = UUID.randomUUID()),
            createTestTeam(id = UUID.randomUUID())
        )

        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns true
        coEvery { teamRepository.findByCompany(testCompanyId, 10, 0) } returns teams
        coEvery { companyRepository.findById(testCompanyId) } returns createTestCompany()
        coEvery { userRepository.findById(any()) } returns createTestUser()
        coEvery { teamMemberRepository.countByTeam(any()) } returns 3

        // Act
        val result = teamService.getCompanyTeams(testCompanyId, testUserId, 1, 10)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getCompanyTeams - should fail when user not in company`() = runBlocking {
        // Arrange
        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns false

        // Act
        val result = teamService.getCompanyTeams(testCompanyId, testUserId, 1, 10)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User is not a member of this company", result.exceptionOrNull()?.message)
    }

    // ============== Тесты getUserTeams ==============

    @Test
    fun `getUserTeams - should return user teams`() = runBlocking {
        // Arrange
        val teams = listOf(
            createTestTeam(id = UUID.randomUUID()),
            createTestTeam(id = UUID.randomUUID())
        )

        coEvery { teamRepository.findByUser(testUserId, null) } returns teams
        coEvery { companyRepository.findById(testCompanyId) } returns createTestCompany()
        coEvery { userRepository.findById(any()) } returns createTestUser()
        coEvery { teamMemberRepository.countByTeam(any()) } returns 3

        // Act
        val result = teamService.getUserTeams(testUserId, null)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getUserTeams - should filter by company`() = runBlocking {
        // Arrange
        coEvery { teamRepository.findByUser(testUserId, testCompanyId) } returns emptyList()

        // Act
        teamService.getUserTeams(testUserId, testCompanyId)

        // Assert
        coVerify(exactly = 1) { teamRepository.findByUser(testUserId, testCompanyId) }
    }

    // ============== Тесты searchTeams ==============

    @Test
    fun `searchTeams - should return search results`() = runBlocking {
        // Arrange
        val teams = listOf(
            createTestTeam(id = UUID.randomUUID(), name = "Backend Team"),
            createTestTeam(id = UUID.randomUUID(), name = "Backend API Team")
        )

        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns true
        coEvery { teamRepository.search(testCompanyId, "backend", 10) } returns teams
        coEvery { companyRepository.findById(testCompanyId) } returns createTestCompany()
        coEvery { userRepository.findById(any()) } returns createTestUser()
        coEvery { teamMemberRepository.countByTeam(any()) } returns 3

        // Act
        val result = teamService.searchTeams(testCompanyId, testUserId, "backend")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `searchTeams - should fail when user not in company`() = runBlocking {
        // Arrange
        coEvery { companyMemberRepository.exists(testCompanyId, testUserId) } returns false

        // Act
        val result = teamService.searchTeams(testCompanyId, testUserId, "test")

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User is not a member of this company", result.exceptionOrNull()?.message)
    }

    // ============== Тесты addMembers ==============

    @Test
    fun `addMembers - should add members to team`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val member = createTestTeamMember(role = TeamRole.ADMIN)
        val newMemberIds = listOf(UUID.randomUUID(), UUID.randomUUID())
        val request = AddTeamMembersRequest(
            userIds = newMemberIds,
            role = TeamRole.MEMBER
        )

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns member
        coEvery { teamMemberRepository.countByTeam(testTeamId) } returns 5
        coEvery { companyMemberRepository.exists(testCompanyId, any()) } returns true
        coEvery { teamMemberRepository.exists(testTeamId, any()) } returns false
        coEvery { teamMemberRepository.addAll(any()) } answers { firstArg() }

        // Act
        val result = teamService.addMembers(testTeamId, testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)

        coVerify(exactly = 1) { teamMemberRepository.addAll(any()) }
        coVerify(exactly = 1) { teamRepository.incrementMemberCount(testTeamId) }
    }

    @Test
    fun `addMembers - should fail when user not in company`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val member = createTestTeamMember(role = TeamRole.ADMIN)
        val targetUserId = UUID.randomUUID()
        val request = AddTeamMembersRequest(
            userIds = listOf(targetUserId),
            role = TeamRole.MEMBER
        )

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns member
        coEvery { teamMemberRepository.countByTeam(testTeamId) } returns 5
        coEvery { companyMemberRepository.exists(testCompanyId, targetUserId) } returns false

        // Act
        val result = teamService.addMembers(testTeamId, testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User $targetUserId is not a member of the company", result.exceptionOrNull()?.message)
    }

    // ============== Тесты getTeamMembers ==============

    @Test
    fun `getTeamMembers - should return team members`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val members = listOf(
            createTestTeamMember(id = UUID.randomUUID(), userId = UUID.randomUUID()),
            createTestTeamMember(id = UUID.randomUUID(), userId = UUID.randomUUID())
        )

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.exists(testTeamId, testUserId) } returns true
        coEvery { teamMemberRepository.findByTeam(testTeamId, 20, 0) } returns members
        coEvery { userRepository.findById(any()) } returns createTestUser()

        // Act
        val result = teamService.getTeamMembers(testTeamId, testUserId, 1, 20)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getTeamMembers - should fail when access denied`() = runBlocking {
        // Arrange
        val team = createTestTeam(visibility = TeamVisibility.PRIVATE)

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.exists(testTeamId, testUserId) } returns false

        // Act
        val result = teamService.getTeamMembers(testTeamId, testUserId, 1, 20)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Access denied", result.exceptionOrNull()?.message)
    }

    // ============== Тесты getTeamMember ==============

    @Test
    fun `getTeamMember - should return specific member`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val member = createTestTeamMember(id = testMemberId, userId = testTargetUserId)

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.exists(testTeamId, testUserId) } returns true
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testTargetUserId) } returns member
        coEvery { userRepository.findById(testTargetUserId) } returns createTestUser()
        coEvery { userRepository.findById(testUserId) } returns createTestUser()

        // Act
        val result = teamService.getTeamMember(testTeamId, testUserId, testTargetUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(testTargetUserId.toString(), result.getOrNull()?.userId)
    }

    @Test
    fun `getTeamMember - should fail when member not found`() = runBlocking {
        // Arrange
        val team = createTestTeam()

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.exists(testTeamId, testUserId) } returns true
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testTargetUserId) } returns null

        // Act
        val result = teamService.getTeamMember(testTeamId, testUserId, testTargetUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User is not a member of this team", result.exceptionOrNull()?.message)
    }

    // ============== Тесты updateMemberRole ==============

    @Test
    fun `updateMemberRole - should update member role`() = runBlocking {
        // Arrange
        val currentUser = createTestTeamMember(id = UUID.randomUUID(), role = TeamRole.LEAD)
        val targetUser = createTestTeamMember(id = testMemberId, role = TeamRole.MEMBER)

        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns currentUser
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testTargetUserId) } returns targetUser
        coEvery { teamMemberRepository.updateRole(testMemberId, TeamRole.ADMIN) } returns true

        // Act
        val result = teamService.updateMemberRole(testTeamId, testUserId, testTargetUserId, TeamRole.ADMIN)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { teamMemberRepository.updateRole(testMemberId, TeamRole.ADMIN) }
    }

    @Test
    fun `updateMemberRole - should fail when admin tries to change lead`() = runBlocking {
        // Arrange
        val currentUser = createTestTeamMember(role = TeamRole.ADMIN)
        val targetUser = createTestTeamMember(role = TeamRole.LEAD)

        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns currentUser
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testTargetUserId) } returns targetUser

        // Act
        val result = teamService.updateMemberRole(testTeamId, testUserId, testTargetUserId, TeamRole.ADMIN)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Only team lead can change lead's role", result.exceptionOrNull()?.message)
    }

    // ============== Тесты removeMember ==============

    @Test
    fun `removeMember - should remove member`() = runBlocking {
        // Arrange
        val currentUser = createTestTeamMember(role = TeamRole.LEAD)
        val targetUser = createTestTeamMember(id = testMemberId, role = TeamRole.MEMBER)

        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns currentUser
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testTargetUserId) } returns targetUser
        coEvery { teamMemberRepository.remove(testMemberId) } returns true

        // Act
        val result = teamService.removeMember(testTeamId, testUserId, testTargetUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { teamMemberRepository.remove(testMemberId) }
        coVerify(exactly = 1) { teamRepository.decrementMemberCount(testTeamId) }
    }

    @Test
    fun `removeMember - should fail when trying to remove lead`() = runBlocking {
        // Arrange
        val currentUser = createTestTeamMember(role = TeamRole.ADMIN)
        val targetUser = createTestTeamMember(role = TeamRole.LEAD)

        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns currentUser
        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testTargetUserId) } returns targetUser

        // Act
        val result = teamService.removeMember(testTeamId, testUserId, testTargetUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Cannot remove team lead", result.exceptionOrNull()?.message)
    }

    // ============== Тесты leaveTeam ==============

    @Test
    fun `leaveTeam - should allow member to leave`() = runBlocking {
        // Arrange
        val member = createTestTeamMember(id = testMemberId, role = TeamRole.MEMBER)

        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns member
        coEvery { teamMemberRepository.remove(testMemberId) } returns true

        // Act
        val result = teamService.leaveTeam(testTeamId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { teamMemberRepository.remove(testMemberId) }
        coVerify(exactly = 1) { teamRepository.decrementMemberCount(testTeamId) }
    }

    @Test
    fun `leaveTeam - should fail when lead tries to leave`() = runBlocking {
        // Arrange
        val member = createTestTeamMember(role = TeamRole.LEAD)

        coEvery { teamMemberRepository.findByTeamAndUser(testTeamId, testUserId) } returns member

        // Act
        val result = teamService.leaveTeam(testTeamId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Team lead cannot leave. Transfer leadership first.", result.exceptionOrNull()?.message)
    }

    // ============== Тесты getTeamStats ==============

    @Test
    fun `getTeamStats - should return team stats`() = runBlocking {
        // Arrange
        val team = createTestTeam()
        val stats = TeamMemberStats(
            totalMembers = 10,
            leads = 1,
            admins = 2,
            members = 7,
            guests = 0,
            activeToday = 5,
            activeThisWeek = 8
        )

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.exists(testTeamId, testUserId) } returns true
        coEvery { teamMemberRepository.getStats(testTeamId) } returns stats

        // Act
        val result = teamService.getTeamStats(testTeamId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull()?.totalMembers)
        assertEquals(1, result.getOrNull()?.leads)
    }

    @Test
    fun `getTeamStats - should fail when access denied`() = runBlocking {
        // Arrange
        val team = createTestTeam(visibility = TeamVisibility.PRIVATE)

        coEvery { teamRepository.findById(testTeamId) } returns team
        coEvery { teamMemberRepository.exists(testTeamId, testUserId) } returns false

        // Act
        val result = teamService.getTeamStats(testTeamId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Access denied", result.exceptionOrNull()?.message)
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
        companySettings = CompanySettings(),
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

    private fun createTestTeam(
        id: UUID = testTeamId,
        name: String = "Test Team",
        visibility: TeamVisibility = TeamVisibility.PRIVATE
    ): Team = Team(
        id = id,
        companyId = testCompanyId,
        name = name,
        description = null,
        avatar = null,
        status = TeamStatus.ACTIVE,
        visibility = visibility,
        leadId = testUserId,
        settings = TeamSettings(),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        archivedAt = null,
        currentMembers = 5
    )

    private fun createTestTeamMember(
        id: UUID = testMemberId,
        userId: UUID = testUserId,
        role: TeamRole = TeamRole.MEMBER
    ): TeamMember = TeamMember(
        id = id,
        teamId = testTeamId,
        userId = userId,
        role = role,
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