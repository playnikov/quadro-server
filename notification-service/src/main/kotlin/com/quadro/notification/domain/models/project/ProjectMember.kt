package com.quadro.notification.domain.models.project

import java.util.UUID

data class ProjectMember(
    val projectId: UUID,
    val userId: UUID,
    val role: ProjectRole
)

enum class ProjectRole {
    OWNER, ADMIN, MEMBER
}