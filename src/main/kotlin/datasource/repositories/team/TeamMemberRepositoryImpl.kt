package com.quadro.datasource.repositories.team

import com.quadro.datasource.entities.TeamEntity
import com.quadro.datasource.entities.TeamMemberEntity
import com.quadro.datasource.entities.TeamMembersTable
import com.quadro.datasource.entities.TeamRoleDb
import com.quadro.datasource.mappers.TeamMemberMapper
import com.quadro.domain.models.team.TeamMember
import com.quadro.domain.models.team.TeamMemberStats
import com.quadro.domain.models.team.TeamRole
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TeamMemberRepositoryImpl : TeamMemberRepository {
    override suspend fun add(member: TeamMember): TeamMember = newSuspendedTransaction {
        TeamMemberMapper.toDomain(TeamMemberMapper.toEntity(member))
    }

    override suspend fun addAll(members: List<TeamMember>): List<TeamMember> = newSuspendedTransaction {
        members.map { member ->
            val entity = TeamMemberMapper.toEntity(member)
            TeamMemberMapper.toDomain(entity)
        }
    }

    override suspend fun findById(id: UUID): TeamMember? = newSuspendedTransaction {
        TeamMemberEntity.findById(id)?.let { TeamMemberMapper.toDomain(it) }
    }

    override suspend fun findByTeamAndUser(
        teamId: UUID,
        userId: UUID
    ): TeamMember? = newSuspendedTransaction {
        TeamMemberEntity.find {
            (TeamMembersTable.teamId eq teamId) and
                    (TeamMembersTable.userId eq userId)
        }.firstOrNull()?.let { TeamMemberMapper.toDomain(it) }
    }

    override suspend fun findByTeam(
        teamId: UUID,
        limit: Int,
        offset: Int
    ): List<TeamMember> = newSuspendedTransaction {
        TeamMemberEntity.find { TeamMembersTable.teamId eq teamId }
            .orderBy(TeamMembersTable.joinedAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map { TeamMemberMapper.toDomain(it) }
    }

    override suspend fun findByUser(
        userId: UUID,
        companyId: UUID?
    ): List<TeamMember> = newSuspendedTransaction {
        val query = TeamMemberEntity.find { TeamMembersTable.userId eq userId }

        if (companyId != null) {
            query.filter { member ->
                TeamEntity.findById(member.teamId)?.companyId == companyId
            }
        } else {
            query.toList()
        }.map { TeamMemberMapper.toDomain(it) }
    }

    override suspend fun updateRole(id: UUID, role: TeamRole): Boolean = newSuspendedTransaction {
        TeamMemberEntity.findById(id)?.apply {
            this.role = when (role) {
                TeamRole.LEAD -> TeamRoleDb.LEAD
                TeamRole.ADMIN -> TeamRoleDb.ADMIN
                TeamRole.MEMBER -> TeamRoleDb.MEMBER
                TeamRole.GUEST -> TeamRoleDb.GUEST
            }
        } != null
    }

    override suspend fun updateLastActive(id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun remove(id: UUID): Boolean = newSuspendedTransaction {
        TeamMemberEntity.findById(id)?.delete() != null
    }

    override suspend fun removeByTeamAndUser(teamId: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        val member = findByTeamAndUser(teamId, userId)
        if (member != null) {
            TeamMemberEntity.findById(member.id)?.delete() != null
        } else false
    }

    override suspend fun removeAllByTeam(teamId: UUID): Int = newSuspendedTransaction {
        val members = TeamMemberEntity.find { TeamMembersTable.teamId eq teamId }.toList()
        members.forEach { it.delete() }
        members.size
    }

    override suspend fun countByTeam(teamId: UUID): Long = newSuspendedTransaction {
        TeamMemberEntity.find { TeamMembersTable.teamId eq teamId }.count()
    }

    override suspend fun countByUser(userId: UUID, companyId: UUID?): Long = newSuspendedTransaction {
        val members = TeamMemberEntity.find { TeamMembersTable.userId eq userId }

        if (companyId != null) {
            members.count { member ->
                TeamEntity.findById(member.teamId)?.companyId == companyId
            }.toLong()
        } else {
            members.count()
        }
    }


    override suspend fun exists(teamId: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        !TeamMemberEntity.find {
            (TeamMembersTable.teamId eq teamId) and
                    (TeamMembersTable.userId eq userId)
        }.empty()
    }

    override suspend fun isUserInRole(
        teamId: UUID,
        userId: UUID,
        role: TeamRole
    ): Boolean = newSuspendedTransaction {
        !TeamMemberEntity.find {
            (TeamMembersTable.teamId eq teamId) and
                    (TeamMembersTable.userId eq userId) and
                    (TeamMembersTable.role eq when (role) {
                        TeamRole.LEAD -> TeamRoleDb.LEAD
                        TeamRole.ADMIN -> TeamRoleDb.ADMIN
                        TeamRole.MEMBER -> TeamRoleDb.MEMBER
                        TeamRole.GUEST -> TeamRoleDb.GUEST
                    })
        }.empty()
    }

    override suspend fun getTeamLeads(teamId: UUID): List<TeamMember> = newSuspendedTransaction {
        TeamMemberEntity.find {
            (TeamMembersTable.teamId eq teamId) and
                    (TeamMembersTable.role eq TeamRoleDb.LEAD)
        }.map { TeamMemberMapper.toDomain(it) }
    }

    override suspend fun getTeamAdmins(teamId: UUID): List<TeamMember> = newSuspendedTransaction {
        TeamMemberEntity.find {
            (TeamMembersTable.teamId eq teamId) and
                    (TeamMembersTable.role eq TeamRoleDb.ADMIN)
        }.map { TeamMemberMapper.toDomain(it) }
    }

    override suspend fun getActiveToday(teamId: UUID): List<TeamMember> {
        TODO("Not yet implemented")
    }

    override suspend fun getStats(teamId: UUID): TeamMemberStats {
        TODO("Not yet implemented")
    }
}