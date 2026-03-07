package com.quadro.domain.services.team

import com.quadro.datasource.repositories.company.CompanyMemberRepository
import com.quadro.datasource.repositories.company.CompanyRepository
import com.quadro.datasource.repositories.team.TeamMemberRepository
import com.quadro.datasource.repositories.team.TeamRepository
import com.quadro.datasource.repositories.users.UserRepository
import com.quadro.domain.models.company.CompanyRole
import com.quadro.domain.models.team.AddTeamMembersRequest
import com.quadro.domain.models.team.Team
import com.quadro.domain.models.team.TeamCreate
import com.quadro.domain.models.team.TeamMember
import com.quadro.domain.models.team.TeamMemberResult
import com.quadro.domain.models.team.TeamMemberStats
import com.quadro.domain.models.team.TeamPermissions
import com.quadro.domain.models.team.TeamResult
import com.quadro.domain.models.team.TeamRole
import com.quadro.domain.models.team.TeamSettings
import com.quadro.domain.models.team.TeamStatus
import com.quadro.domain.models.team.TeamUpdate
import com.quadro.domain.models.team.TeamVisibility
import org.slf4j.LoggerFactory
import java.util.*

class TeamServiceImpl(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val companyRepository: CompanyRepository,
    private val companyMemberRepository: CompanyMemberRepository,
    private val userRepository: UserRepository
) : TeamService {
    private val logger = LoggerFactory.getLogger(javaClass)
    override suspend fun createTeam(
        userId: UUID,
        request: TeamCreate
    ): Result<TeamResult> {
        return try {
            companyRepository.findById(request.companyId)
                ?: return Result.failure(Exception("Company not found"))

            val companyMember =
                companyMemberRepository.findByCompanyAndUser(request.companyId, userId) ?: return Result.failure(
                    Exception("User is not a member of this company")
                )

            if (!canCreateTeam(companyMember.role)) {
                return Result.failure(Exception("Insufficient permissions to create team"))
            }

            if (teamRepository.existsByName(request.companyId, request.name)) {
                return Result.failure(Exception("Team with this name already exists in company"))
            }

            val now = System.currentTimeMillis()

            val team = Team(
                id = UUID.randomUUID(),
                companyId = request.companyId,
                name = request.name,
                description = request.description,
                avatar = request.avatar,
                status = TeamStatus.ACTIVE,
                visibility = request.visibility,
                leadId = userId,
                settings = request.settings ?: TeamSettings(),
                createdAt = now,
                updatedAt = now,
                archivedAt = null,
                currentMembers = 1 + (request.initialMembers?.size ?: 0)
            )

            val createdTeam = teamRepository.create(team)

            val leadMember = TeamMember(
                id = UUID.randomUUID(),
                teamId = createdTeam.id,
                userId = userId,
                role = TeamRole.LEAD,
                joinedAt = now,
                invitedBy = userId,
                invitedAt = now,
                isActive = true
            )
            teamMemberRepository.add(leadMember)

            request.initialMembers?.let { memberIds ->
                addInitialMembers(createdTeam.id, userId, memberIds, request.companyId)
            }

            logger.info("Team created: ${createdTeam.name} in company: ${request.companyId} by user: $userId")

            Result.success(buildTeamResult(createdTeam, userId))
        } catch (e: Exception) {
            logger.error("Failed to create team", e)
            Result.failure(e)
        }
    }

    override suspend fun getTeam(
        teamId: UUID,
        userId: UUID
    ): Result<TeamResult> {
        return try {
            val team = teamRepository.findById(teamId)
                ?: return Result.failure(Exception("Team not found"))

            if (!canViewTeam(team, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            Result.success(buildTeamResult(team, userId))
        } catch (e: Exception) {
            logger.error("Failed to get team", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTeam(
        teamId: UUID,
        userId: UUID,
        request: TeamUpdate
    ): Result<TeamResult> {
        return try {
            val team = teamRepository.findById(teamId)
                ?: return Result.failure(Exception("Team not found"))

            val member = teamMemberRepository.findByTeamAndUser(teamId, userId)
            if (!canEditTeam(member?.role)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            if (request.name != null && request.name != team.name) {
                if (teamRepository.existsByName(team.companyId, request.name)) {
                    return Result.failure(Exception("Team with this name already exists in company"))
                }
            }

            val updatedTeam = team.copy(
                name = request.name ?: team.name,
                description = request.description ?: team.description,
                avatar = request.avatar ?: team.avatar,
                visibility = request.visibility ?: team.visibility,
                settings = request.settings ?: team.settings,
                status = request.status ?: team.status,
                updatedAt = System.currentTimeMillis()
            )

            val savedTeam = teamRepository.update(updatedTeam)
            logger.info("Team updated: $teamId by user: $userId")

            Result.success(buildTeamResult(savedTeam, userId))
        } catch (e: Exception) {
            logger.error("Failed to update team", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTeam(teamId: UUID, userId: UUID): Result<Unit> {
        return try {
            teamRepository.findById(teamId)
                ?: return Result.failure(Exception("Team not found"))

            val member = teamMemberRepository.findByTeamAndUser(teamId, userId)
            if (member == null || member.role != TeamRole.LEAD) {
                return Result.failure(Exception("Only team lead can delete the team"))
            }

            teamMemberRepository.removeAllByTeam(teamId)

            teamRepository.delete(teamId)

            logger.info("Team deleted: $teamId by user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to delete team", e)
            Result.failure(e)
        }
    }

    override suspend fun archiveTeam(teamId: UUID, userId: UUID): Result<Unit> {
        return try {
            teamRepository.findById(teamId)
                ?: return Result.failure(Exception("Team not found"))

            val member = teamMemberRepository.findByTeamAndUser(teamId, userId)
            if (!canArchiveTeam(member?.role)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            teamRepository.updateStatus(teamId, TeamStatus.ARCHIVED)
            logger.info("Team archived: $teamId by user: $userId")

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to archive team", e)
            Result.failure(e)
        }
    }

    override suspend fun restoreTeam(teamId: UUID, userId: UUID): Result<Unit> {
        return try {
            teamRepository.findById(teamId)
                ?: return Result.failure(Exception("Team not found"))

            val member = teamMemberRepository.findByTeamAndUser(teamId, userId)
            if (!canArchiveTeam(member?.role)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            teamRepository.updateStatus(teamId, TeamStatus.ACTIVE)
            logger.info("Team restored: $teamId by user: $userId")

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to restore team", e)
            Result.failure(e)
        }
    }

    override suspend fun getCompanyTeams(
        companyId: UUID,
        userId: UUID,
        page: Int,
        size: Int
    ): Result<List<TeamResult>> {
        return try {
            if (!companyMemberRepository.exists(companyId, userId)) {
                return Result.failure(Exception("User is not a member of this company"))
            }

            val offset = (page - 1) * size
            val teams = teamRepository.findByCompany(companyId, size, offset)

            val results = teams.map { team ->
                buildTeamResult(team, userId)
            }

            Result.success(results)
        } catch (e: Exception) {
            logger.error("Failed to get company teams", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserTeams(
        userId: UUID,
        companyId: UUID?
    ): Result<List<TeamResult>> {
        return try {
            val teams = teamRepository.findByUser(userId, companyId)
            val results = teams.map { team ->
                buildTeamResult(team, userId)
            }
            Result.success(results)
        } catch (e: Exception) {
            logger.error("Failed to get user teams", e)
            Result.failure(e)
        }
    }

    override suspend fun searchTeams(
        companyId: UUID,
        userId: UUID,
        query: String
    ): Result<List<TeamResult>> {
        return try {
            if (!companyMemberRepository.exists(companyId, userId)) {
                return Result.failure(Exception("User is not a member of this company"))
            }

            val teams = teamRepository.search(companyId, query, 10)
            val results = teams.map { team ->
                buildTeamResult(team, userId)
            }

            Result.success(results)
        } catch (e: Exception) {
            logger.error("Failed to search teams", e)
            Result.failure(e)
        }
    }

    override suspend fun addMembers(
        teamId: UUID,
        userId: UUID,
        request: AddTeamMembersRequest
    ): Result<List<TeamMemberResult>> {
        return try {
            val team = teamRepository.findById(teamId)
                ?: return Result.failure(Exception("Team not found"))

            val member = teamMemberRepository.findByTeamAndUser(teamId, userId)
            if (!canAddMembers(member?.role, team.settings.memberCanInvite)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            val currentCount = teamMemberRepository.countByTeam(teamId)
            if (currentCount + request.userIds.size > 10) {
                return Result.failure(Exception("Team member limit exceeded"))
            }

            val now = System.currentTimeMillis()
            val addedMembers = mutableListOf<TeamMember>()

            for (targetUserId in request.userIds) {
                if (!companyMemberRepository.exists(team.companyId, targetUserId)) {
                    return Result.failure(Exception("User $targetUserId is not a member of the company"))
                }

                if (teamMemberRepository.exists(teamId, targetUserId)) {
                    continue
                }

                val newMember = TeamMember(
                    id = UUID.randomUUID(),
                    teamId = teamId,
                    userId = targetUserId,
                    role = request.role,
                    joinedAt = now,
                    invitedBy = userId,
                    invitedAt = now,
                    isActive = true
                )
                addedMembers.add(newMember)
            }

            val createdMembers = teamMemberRepository.addAll(addedMembers)
            teamRepository.incrementMemberCount(teamId)

            logger.info("Added ${createdMembers.size} members to team: $teamId")

            Result.success(buildMemberResults(createdMembers))
        } catch (e: Exception) {
            logger.error("Failed to add members to team", e)
            Result.failure(e)
        }
    }

    override suspend fun getTeamMembers(
        teamId: UUID,
        userId: UUID,
        page: Int,
        size: Int
    ): Result<List<TeamMemberResult>> {
        return try {
            val team = teamRepository.findById(teamId)
                ?: return Result.failure(Exception("Team not found"))

            if (!canViewTeam(team, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            val offset = (page - 1) * size
            val members = teamMemberRepository.findByTeam(teamId, size, offset)

            Result.success(buildMemberResults(members))
        } catch (e: Exception) {
            logger.error("Failed to get team members", e)
            Result.failure(e)
        }
    }

    override suspend fun getTeamMember(
        teamId: UUID,
        userId: UUID,
        targetUserId: UUID
    ): Result<TeamMemberResult> {
        return try {
            val team = teamRepository.findById(teamId)
                ?: return Result.failure(Exception("Team not found"))

            if (!canViewTeam(team, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            val member = teamMemberRepository.findByTeamAndUser(teamId, targetUserId)
                ?: return Result.failure(Exception("User is not a member of this team"))

            Result.success(buildMemberResult(member))
        } catch (e: Exception) {
            logger.error("Failed to get team member", e)
            Result.failure(e)
        }
    }

    override suspend fun updateMemberRole(
        teamId: UUID,
        userId: UUID,
        targetUserId: UUID,
        role: TeamRole
    ): Result<Unit> {
        return try {
            val currentUserMember = teamMemberRepository.findByTeamAndUser(teamId, userId)
                ?: return Result.failure(Exception("User is not a member of this team"))

            if (!canChangeRole(currentUserMember.role)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            val targetMember = teamMemberRepository.findByTeamAndUser(teamId, targetUserId)
                ?: return Result.failure(Exception("Target user is not a member of this team"))

            if (targetMember.role == TeamRole.LEAD && currentUserMember.role != TeamRole.LEAD) {
                return Result.failure(Exception("Only team lead can change lead's role"))
            }

            if (currentUserMember.role == TeamRole.ADMIN && targetMember.role == TeamRole.ADMIN) {
                return Result.failure(Exception("Admin cannot change another admin's role"))
            }

            teamMemberRepository.updateRole(targetMember.id, role)
            logger.info("Member role updated: $targetUserId to $role in team: $teamId")

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to update member role", e)
            Result.failure(e)
        }
    }

    override suspend fun removeMember(
        teamId: UUID,
        userId: UUID,
        targetUserId: UUID
    ): Result<Unit> {
        return try {
            val currentUserMember = teamMemberRepository.findByTeamAndUser(teamId, userId)
                ?: return Result.failure(Exception("User is not a member of this team"))

            val targetMember = teamMemberRepository.findByTeamAndUser(teamId, targetUserId)
                ?: return Result.failure(Exception("Target user is not a member of this team"))

            if (targetMember.role == TeamRole.LEAD) {
                return Result.failure(Exception("Cannot remove team lead"))
            }

            if (!canRemoveMember(currentUserMember.role, targetMember.role)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            teamMemberRepository.remove(targetMember.id)
            teamRepository.decrementMemberCount(teamId)

            logger.info("Member removed: $targetUserId from team: $teamId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to remove member from team", e)
            Result.failure(e)
        }
    }

    override suspend fun leaveTeam(teamId: UUID, userId: UUID): Result<Unit> {
        return try {
            val member = teamMemberRepository.findByTeamAndUser(teamId, userId)
                ?: return Result.failure(Exception("User is not a member of this team"))

            if (member.role == TeamRole.LEAD) {
                return Result.failure(Exception("Team lead cannot leave. Transfer leadership first."))
            }

            teamMemberRepository.remove(member.id)
            teamRepository.decrementMemberCount(teamId)

            logger.info("User $userId left team: $teamId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to leave team", e)
            Result.failure(e)
        }
    }

    override suspend fun getTeamStats(
        teamId: UUID,
        userId: UUID
    ): Result<TeamMemberStats> {
        return try {
            val team = teamRepository.findById(teamId)
                ?: return Result.failure(Exception("Team not found"))

            if (!canViewTeam(team, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            val stats = teamMemberRepository.getStats(teamId)
            Result.success(stats)
        } catch (e: Exception) {
            logger.error("Failed to get team stats", e)
            Result.failure(e)
        }
    }

    private suspend fun addInitialMembers(teamId: UUID, invitedBy: UUID, memberIds: List<UUID>, companyId: UUID) {
        val now = System.currentTimeMillis()
        val members = memberIds.mapNotNull { userId ->
            if (!companyMemberRepository.exists(companyId, userId)) {
                return@mapNotNull null
            }

            TeamMember(
                id = UUID.randomUUID(),
                teamId = teamId,
                userId = userId,
                role = TeamRole.MEMBER,
                joinedAt = now,
                invitedBy = invitedBy,
                invitedAt = now,
                isActive = true
            )
        }

        if (members.isNotEmpty()) {
            teamMemberRepository.addAll(members)
        }
    }

    private fun canCreateTeam(companyRole: CompanyRole): Boolean {
        return companyRole in listOf(CompanyRole.OWNER, CompanyRole.ADMIN, CompanyRole.MANAGER)
    }

    private suspend fun canViewTeam(team: Team, userId: UUID): Boolean {
        return when (team.visibility) {
            TeamVisibility.PUBLIC -> true
            TeamVisibility.PRIVATE -> teamMemberRepository.exists(team.id, userId)
            TeamVisibility.HIDDEN -> {
                val member = teamMemberRepository.findByTeamAndUser(team.id, userId)
                member?.role in listOf(TeamRole.LEAD, TeamRole.ADMIN)
            }
        }
    }

    private fun canEditTeam(role: TeamRole?): Boolean {
        return role in listOf(TeamRole.LEAD, TeamRole.ADMIN)
    }

    private fun canArchiveTeam(role: TeamRole?): Boolean {
        return role in listOf(TeamRole.LEAD, TeamRole.ADMIN)
    }

    private fun canAddMembers(role: TeamRole?, memberCanInvite: Boolean): Boolean {
        return when (role) {
            TeamRole.LEAD, TeamRole.ADMIN -> true
            TeamRole.MEMBER -> memberCanInvite
            else -> false
        }
    }

    private fun canChangeRole(role: TeamRole?): Boolean {
        return role in listOf(TeamRole.LEAD, TeamRole.ADMIN)
    }

    private fun canRemoveMember(userRole: TeamRole, targetRole: TeamRole): Boolean {
        return when (userRole) {
            TeamRole.LEAD -> true
            TeamRole.ADMIN -> targetRole != TeamRole.ADMIN && targetRole != TeamRole.LEAD
            else -> false
        }
    }

    private suspend fun buildTeamResult(team: Team, userId: UUID): TeamResult {
        val company = companyRepository.findById(team.companyId)!!
        val lead = userRepository.findById(team.leadId)!!
        val memberCount = teamMemberRepository.countByTeam(team.id).toInt()
        val projectCount = 0
        val isMember = teamMemberRepository.exists(team.id, userId)
        val userMember = if (isMember) teamMemberRepository.findByTeamAndUser(team.id, userId) else null

        val permissions = TeamPermissions.fromRole(userMember?.role)

        return TeamResult.fromTeam(
            team = team,
            companyName = company.name,
            leadName = lead.username,
            leadEmail = lead.email,
            memberCount = memberCount,
            projectCount = projectCount,
            isMember = isMember,
            userRole = userMember?.role,
            permissions = permissions
        )
    }

    private suspend fun buildMemberResult(member: TeamMember): TeamMemberResult {
        val user = userRepository.findById(member.userId)!!
        val inviter = userRepository.findById(member.invitedBy)!!
        val currentUserRole = teamMemberRepository.findByTeamAndUser(member.teamId, member.userId)?.role

        return TeamMemberResult(
            id = member.id.toString(),
            teamId = member.teamId.toString(),
            userId = member.userId.toString(),
            userEmail = user.email,
            userName = user.lastName + " " + user.firstName,
            userAvatar = user.avatar,
            role = member.role.toString(),
            joinedAt = member.joinedAt,
            invitedBy = member.invitedBy.toString(),
            invitedByEmail = inviter.email,
            canEdit = canChangeRole(currentUserRole),
            canRemove = canRemoveMember(currentUserRole ?: TeamRole.GUEST, member.role)
        )
    }

    private suspend fun buildMemberResults(members: List<TeamMember>): List<TeamMemberResult> {
        return members.map { buildMemberResult(it) }
    }
}