package com.quadro.task.infrastructure.database.mappers.team

import com.quadro.task.domain.models.team.TeamProject
import com.quadro.task.domain.models.team.TeamProjectRole
import com.quadro.task.infrastructure.database.entities.team.TeamProjectsEntity

object TeamProjectMapper {
    fun toDomain(entity: TeamProjectsEntity): TeamProject = TeamProject(
        teamId = entity.teamId,
        projectId = entity.projectId,
        role = TeamProjectRole.valueOf(entity.role)
    )

    fun toEntity(domain: TeamProject): TeamProjectsEntity =
        TeamProjectsEntity.new {
            this.projectId = domain.projectId
            this.teamId = domain.teamId
            this.role = domain.role.name
        }

    fun updateEntity(entity: TeamProjectsEntity, domain: TeamProject) {
        entity.projectId = domain.projectId
        entity.teamId = domain.teamId
        entity.role = domain.role.name
    }
}