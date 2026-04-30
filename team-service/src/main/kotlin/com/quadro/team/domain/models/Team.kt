package com.quadro.team.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant
import kotlin.toString

@Serializable
enum class TeamRole {
    MEMBER,     // Может просматривать задачи и комментировать
    LEAD,       // Может назначать задачи и управлять спринтами
    MANAGER     // Полный контроль над командой и проектами
}

@Serializable
enum class TeamStatus {
    ACTIVE, ARCHIVED, DISBAND
}

@Serializable
enum class TeamVisibility {
    PUBLIC, PRIVATE, HIDDEN
}

@Serializable
enum class TeamProjectRole {
    VIEWER,       // только просмотр задач
    CONTRIBUTOR,  // может брать задачи
    ASSIGNEE,     // задачи назначаются команде
    MANAGER       // управляет задачами в проекте
}

data class Team(
    val id: UUID,
    val name: String,
    val description: String?,
    val avatar: String?,
    val status: TeamStatus,
    val visibility: TeamVisibility,
    val createdBy: UUID,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class TeamCreate(
    val name: String,
    val description: String? = null,
    val avatar: String? = null,
    val leadId: UUID,
    val visibility: TeamVisibility = TeamVisibility.PUBLIC,
    val initialMembers: List<UUID>? = null
) {
    fun validate() {
        require(name.isNotBlank()) { "Team name cannot be blank" }
        require(name.length in 2..50) { "Name must be 2–50 characters" }
        require((initialMembers?.distinct()?.size ?: 0) == (initialMembers?.size ?: 0)) { "Duplicate members in initialMembers" }
    }
}

data class TeamUpdate(
    val name: String? = null,
    val description: String? = null,
    val avatar: String? = null,
    val leadId: String? = null,
    val visibility: TeamVisibility? = null,
    val status: TeamStatus? = null
) {
    fun validate() {
        require(name?.length in 2..50) { "Name: 2–50 chars" }
    }
}

@Serializable
data class TeamResponse(
    val id: String,
    val name: String,
    val description: String?,
    val avatar: String?,
    val status: TeamStatus,
    val visibility: TeamVisibility,
    val createdBy: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val members: List<TeamMemberResponse> = emptyList(),
    val projects: List<TeamProjectBindingResponse> = emptyList()
) {
    companion object {
        fun from(team: Team): TeamResponse = TeamResponse(
            id = team.id.toString(),
            name = team.name,
            description = team.description,
            avatar = team.avatar,
            status = team.status,
            visibility = team.visibility,
            createdAt = team.createdAt,
            updatedAt = team.updatedAt,
            createdBy = team.createdBy.toString()
        )
    }
}

data class TeamProjectBinding(
    val id: UUID,
    val teamId: UUID,
    val projectId: UUID,
    val role: TeamProjectRole,
    val boundAt: Instant,
    val boundBy: UUID,
)

@Serializable
data class TeamProjectBindingResponse(
    val id: String,
    val teamId: String,
    val projectId: String,
    val role: TeamProjectRole,
    val boundAt: Instant,
    val boundBy: String
) {
    companion object {
        fun from(bind: TeamProjectBinding): TeamProjectBindingResponse = TeamProjectBindingResponse(
            id = bind.id.toString(),
            teamId = bind.teamId.toString(),
            projectId = bind.projectId.toString(),
            role = bind.role,
            boundAt = bind.boundAt,
            boundBy = bind.boundBy.toString()
        )
    }
}