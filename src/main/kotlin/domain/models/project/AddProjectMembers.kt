package com.quadro.domain.models.project

import java.util.UUID

data class AddProjectMembers(
    val userIds: List<UUID>,
    val role: ProjectRole = ProjectRole.MEMBER
)

data class UpdateMemberRole(
    val role: ProjectRole
)