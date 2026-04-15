package com.quadro.team.infrastructure.database.repositories

import com.quadro.team.domain.models.TeamMember
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.domain.repositories.TeamMemberRepository
import com.quadro.team.infrastructure.database.entities.TeamEntity
import com.quadro.team.infrastructure.database.entities.TeamMembersEntity
import com.quadro.team.infrastructure.database.entities.TeamMembersTable
import com.quadro.team.infrastructure.database.entities.TeamsTable
import com.quadro.team.infrastructure.database.mappers.TeamMembersMapper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TeamMemberRepositoryImpl : TeamMemberRepository {
    override suspend fun findByTeamAndUser(
        teamId: UUID,
        userId: UUID
    ): TeamMember? = newSuspendedTransaction {
        TeamMembersEntity.find {
            (TeamMembersTable.teamId eq teamId) and
                    (TeamMembersTable.userId eq userId)
        }.firstOrNull()?.let { TeamMembersMapper.toDomain(it) }
    }

    override suspend fun findByTeam(teamId: UUID): List<TeamMember> = newSuspendedTransaction {
        TeamMembersEntity.find { TeamMembersTable.teamId eq teamId }
            .map { TeamMembersMapper.toDomain(it) }
    }

    override suspend fun exists(teamId: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        !TeamMembersEntity.find {
            (TeamMembersTable.teamId eq teamId) and
                    (TeamMembersTable.userId eq userId)
        }.empty()
    }

    override suspend fun add(member: TeamMember): TeamMember = newSuspendedTransaction {
        TeamMembersMapper.toDomain(TeamMembersMapper.toEntity(member))
    }

    override suspend fun remove(id: UUID): Boolean = newSuspendedTransaction {
        TeamMembersEntity.findById(id)?.delete() != null
    }

    override suspend fun updateRole(id: UUID, role: TeamRole): Unit = newSuspendedTransaction {
        TeamMembersEntity.findById(id)?.apply {
            this.role = role.name
        } != null
    }
}