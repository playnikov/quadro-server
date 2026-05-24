package com.quadro.task.infrastructure.database.mappers.project

import com.quadro.task.domain.models.project.ProjectMember
import com.quadro.task.domain.models.project.MemberRole
import com.quadro.task.infrastructure.database.entities.project.ProjectMemberEntity

object ProjectMemberMapper {
    fun toDomain(entity: ProjectMemberEntity): ProjectMember = ProjectMember(
        projectId = entity.projectId,
        userId = entity.userId,
        role = entity.role
    )

    fun newEntity(domain: ProjectMember): ProjectMemberEntity =
        ProjectMemberEntity.new {
            this.projectId = domain.projectId
            this.userId = domain.userId
            this.role = domain.role
        }

    fun updateEntity(entity: ProjectMemberEntity, domain: ProjectMember) {
        entity.projectId = domain.projectId
        entity.userId = domain.userId
        entity.role = domain.role
    }
}