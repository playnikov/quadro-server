package com.quadro.team.domain.services

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.TeamMemberAddedEvent
import com.quadro.shared.data.messaging.events.TeamMemberRemovedEvent
import com.quadro.shared.data.messaging.events.TeamMemberUpdatedEvent
import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.Team
import com.quadro.team.domain.models.TeamMember
import com.quadro.team.domain.models.TeamMemberResponse
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.domain.repositories.TeamMemberRepository
import com.quadro.team.domain.repositories.TeamRepository
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class TeamMemberServiceImpl(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val eventProducer: EventProducer
) : TeamMemberService {
    private val logger = LoggerFactory.getLogger(javaClass)

    private suspend fun checkTeamExists(teamId: UUID): Team =
        teamRepository.findById(teamId)
            ?: throw DomainException.NotFound("Team", teamId.toString())

    override suspend fun getMembers(teamId: UUID): List<TeamMemberResponse> {
        checkTeamExists(teamId)
        return teamMemberRepository.findByTeam(teamId).map { TeamMemberResponse.from(it) }
    }

    override suspend fun addMember(
        teamId: UUID,
        userId: UUID,
        role: TeamRole,
        requesterId: UUID
    ): TeamMemberResponse {
        checkTeamExists(teamId)

        if (teamMemberRepository.exists(teamId, userId)) throw DomainException.AlreadyExists("User already in team")
        val member = teamMemberRepository.add(
            TeamMember(
                id = UUID.randomUUID(),
                teamId = teamId,
                userId = userId,
                role = role,
                joinedAt = Clock.System.now(),
                invitedBy = requesterId,
                isActive = true,
                invitedAt = Clock.System.now()
            )
        )

        eventProducer.publish(
            topic = KafkaTopics.TEAM_MEMBER_ADDED,
            key = member.id.toString(),
            event = TeamMemberAddedEvent(
                teamId = member.id.toString(),
                userId = member.userId.toString(),
                role = member.role.name
            )
        )

        return TeamMemberResponse.from(member)
    }

    override suspend fun removeMember(teamId: UUID, memberId: UUID, requesterId: UUID) {
        checkTeamExists(teamId)

        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        if(!teamMemberRepository.exists(memberId, requesterId)) throw DomainException.NotFound("User", requesterId.toString())
        teamMemberRepository.remove(memberId)

        eventProducer.publish(
            topic = KafkaTopics.TEAM_MEMBER_REMOVED,
            key = memberId.toString(),
            event = TeamMemberRemovedEvent(
                teamId = teamId.toString(),
                userId = memberId.toString()
            )
        )
    }

    override suspend fun changeRole(
        teamId: UUID,
        memberId: UUID,
        role: TeamRole,
        requesterId: UUID
    ) {
        checkTeamExists(teamId)

        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        if(!teamMemberRepository.exists(memberId, requesterId)) throw DomainException.NotFound("User", requesterId.toString())
        teamMemberRepository.updateRole(memberId, role)

        eventProducer.publish(
            topic = KafkaTopics.TEAM_MEMBER_UPDATED,
            key = memberId.toString(),
            event = TeamMemberUpdatedEvent(
                teamId = teamId.toString(),
                userId = memberId.toString(),
                role = role.name
            )
        )
    }

}