package com.quadro.team.domain.services

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.TeamCreatedEvent
import com.quadro.shared.data.messaging.events.TeamDeletedEvent
import com.quadro.shared.data.messaging.events.TeamMemberAddedEvent
import com.quadro.shared.data.messaging.events.TeamUpdatedEvent
import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.Project
import com.quadro.team.domain.models.Team
import com.quadro.team.domain.models.TeamCreate
import com.quadro.team.domain.models.TeamMember
import com.quadro.team.domain.models.TeamMemberResponse
import com.quadro.team.domain.models.TeamProjectBinding
import com.quadro.team.domain.models.TeamProjectBindingResponse
import com.quadro.team.domain.models.TeamResponse
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.domain.models.TeamStatus
import com.quadro.team.domain.models.TeamUpdate
import com.quadro.team.domain.models.User
import com.quadro.team.domain.repositories.ProjectRepository
import com.quadro.team.domain.repositories.TeamMemberRepository
import com.quadro.team.domain.repositories.TeamProjectBindingRepository
import com.quadro.team.domain.repositories.TeamRepository
import com.quadro.team.domain.repositories.UserRepository
import io.ktor.client.request.request
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class TeamServiceImpl(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val projectBindingRepository: TeamProjectBindingRepository,
    private val userRepository: UserRepository,
    private val eventProducer: EventProducer
) : TeamService {
    private val logger = LoggerFactory.getLogger(javaClass)

    private suspend fun checkUserExists(userId: UUID): User =
        userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", "ID: $userId")

    override suspend fun create(createdBy: UUID, request: TeamCreate): TeamResponse {
        checkUserExists(createdBy)
        request.validate()

        if (teamRepository.existsByName(request.name)) {
            logger.warn("Attempt to create duplicate team '${request.name}' by user $createdBy")
            throw DomainException.AlreadyExists("Team '${request.name}'")
        }

        val now = Clock.System.now()
        val team = teamRepository.create(
            Team(
                id = UUID.randomUUID(),
                name = request.name,
                description = request.description,
                avatar = request.avatar,
                status = TeamStatus.ACTIVE,
                visibility = request.visibility,
                createdBy = createdBy,
                createdAt = now,
                updatedAt = now
            )
        )
        eventProducer.publish(
            topic = KafkaTopics.TEAM_CREATED,
            key = team.id.toString(),
            event = TeamCreatedEvent(
                teamId = team.id.toString(),
                name = team.name,
                status = team.status.name,
                createdBy = team.createdBy.toString()
            )
        )

        val lead = teamMemberRepository.add(
            TeamMember(
                id = UUID.randomUUID(),
                teamId = team.id,
                userId = request.leadId,
                role = TeamRole.LEAD,
                joinedAt = now,
                invitedBy = createdBy,
                invitedAt = now,
                isActive = true
            )
        )

        eventProducer.publish(
            topic = KafkaTopics.TEAM_MEMBER_ADDED,
            key = lead.id.toString(),
            event = TeamMemberAddedEvent(
                teamId = team.id.toString(),
                userId = lead.userId.toString(),
                role = lead.role.name,
                isActive = lead.isActive
            )
        )

        request.initialMembers?.let { members ->
            val invalidCount = members.size - members.distinct().size
            if (invalidCount > 0) {
                logger.warn("Duplicate members in initialMembers list")
            }

            members.filter { it != request.leadId }.forEach { memberId ->
                checkUserExists(memberId)
                val member = teamMemberRepository.add(
                    TeamMember(
                        id = UUID.randomUUID(),
                        teamId = team.id,
                        userId = memberId,
                        role = TeamRole.MEMBER,
                        joinedAt = now,
                        invitedBy = createdBy,
                        invitedAt = now,
                        isActive = true
                    )
                )

                eventProducer.publish(
                    topic = KafkaTopics.TEAM_MEMBER_ADDED,
                    key = member.id.toString(),
                    event = TeamMemberAddedEvent(
                        teamId = team.id.toString(),
                        userId = member.userId.toString(),
                        role = member.role.name,
                        isActive = member.isActive
                    )
                )
            }
        }

        logger.info("Created Team id=${team.id}, name='${request.name}', createdBy=$createdBy")

        val response = TeamResponse.from(team)
        val (members, projects) = getMembersAndProjects(team.id)
        return response.copy(
            members = members,
            projects = projects
        )
    }

    override suspend fun getById(id: UUID): TeamResponse {
        val team = teamRepository.findById(id) ?: throw DomainException.NotFound("Team", id.toString())
        logger.info("Retrieved team: id=$id, name=${team.name}")
        val response = TeamResponse.from(team)
        val (members, projects) = getMembersAndProjects(id)
        return response.copy(
            members = members,
            projects = projects
        )
    }

    override suspend fun getAll(page: Int, size: Int): List<TeamResponse> =
        teamRepository.findAll(page, size)
            .map {
                val (members, projects) = getMembersAndProjects(it.id)
                val response = TeamResponse.from(it)
                response.copy(
                    members = members,
                    projects = projects,
                )
            }

    override suspend fun update(id: UUID, request: TeamUpdate, requesterId: UUID): TeamResponse {
        val team = teamRepository.findById(id) ?: throw DomainException.NotFound("Team", id.toString())
        checkUserExists(requesterId)
        val teamUpdate = teamRepository.update(
            team.copy(
                name = request.name?.trim() ?: team.name,
                description = request.description?.trim() ?: team.description,
                avatar = request.avatar?.trim() ?: team.avatar,
                visibility = request.visibility ?: team.visibility,
                status = request.status ?: team.status
            )
        )

        eventProducer.publish(
            topic = KafkaTopics.TEAM_UPDATED,
            key = team.id.toString(),
            event = TeamUpdatedEvent(
                teamId = teamUpdate.id.toString(),
                name = teamUpdate.name,
                status = teamUpdate.status.name,
                updatedBy = requesterId.toString()
            )
        )

        logger.info("Updated team id=$id, changes: name=${request.name ?: "unchanged"}, leadId=${request.leadId ?: "unchanged"}")
        val response = TeamResponse.from(teamUpdate)
        val (members, projects) = getMembersAndProjects(teamUpdate.id)
        return response.copy(
            members = members,
            projects = projects
        )
    }

    override suspend fun delete(id: UUID, requesterId: UUID) {
        val team = teamRepository.findById(id) ?: throw DomainException.NotFound("Team", id.toString())
        checkUserExists(requesterId)
        if (team.createdBy != requesterId) throw DomainException.Forbidden("Only creator can delete team")
        teamRepository.delete(id)

        eventProducer.publish(
            topic = KafkaTopics.TEAM_DELETED,
            key = team.id.toString(),
            event = TeamDeletedEvent(
                teamId = id.toString(),
                deletedBy = requesterId.toString()
            )
        )

        logger.info("Deleted team id=$id by user $requesterId")
    }

    private suspend fun getMembersAndProjects(teamId: UUID): Pair<List<TeamMemberResponse>, List<TeamProjectBindingResponse>> {
        val members = teamMemberRepository.findByTeam(teamId).map { teamMember ->
            TeamMemberResponse.from(teamMember)
        }
        val projects = projectBindingRepository.findByTeam(teamId).map { binding ->
            TeamProjectBindingResponse.from(binding)
        }

        return members to projects
    }
}