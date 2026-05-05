package com.quadro.task.infrastructure.database.repositories.team

import com.quadro.task.domain.models.team.TeamMember
import com.quadro.task.domain.repositories.team.TeamMemberRepository
import com.quadro.task.infrastructure.database.entities.team.TeamMembersEntity
import com.quadro.task.infrastructure.database.entities.team.TeamMembersTable
import com.quadro.task.infrastructure.database.mappers.team.TeamMembersMapper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TeamMemberRepositoryImpl : TeamMemberRepository {
    override suspend fun upsert(member: TeamMember): Unit = newSuspendedTransaction {
        val existing = TeamMembersEntity.find {
            (TeamMembersTable.teamId eq member.teamId) and
                    (TeamMembersTable.userId eq member.userId)
        }.firstOrNull()
        if (existing != null) {
            TeamMembersMapper.updateEntity(existing, member)
        } else {
            TeamMembersMapper.toEntity(member)
        }
    }

    override suspend fun findByTeam(teamId: UUID): List<TeamMember> = newSuspendedTransaction {
        TeamMembersEntity.find { TeamMembersTable.teamId eq teamId }
            .map(TeamMembersMapper::toDomain)
    }

    override suspend fun findByUserId(userId: UUID): List<TeamMember> = newSuspendedTransaction {
        TeamMembersEntity.find { TeamMembersTable.userId eq userId }
            .map(TeamMembersMapper::toDomain)
    }

    override suspend fun deleteByTeam(teamId: UUID): Unit = newSuspendedTransaction {
        TeamMembersEntity.find { TeamMembersTable.teamId eq teamId }
            .forEach { it.delete() }
    }

    override suspend fun delete(teamId: UUID, userId: UUID): Unit = newSuspendedTransaction {
        TeamMembersEntity.find {
            (TeamMembersTable.teamId eq teamId) and
                    (TeamMembersTable.userId eq userId)}
            .firstOrNull()?.delete()
    }
}