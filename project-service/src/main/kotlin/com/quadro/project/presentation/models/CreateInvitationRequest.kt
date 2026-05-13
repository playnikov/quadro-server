package com.quadro.project.presentation.models

import com.quadro.project.domain.models.InviteType
import com.quadro.project.domain.models.ProjectRole
import kotlinx.serialization.Serializable

@Serializable
data class CreateInvitationRequest(
    val role: ProjectRole = ProjectRole.MEMBER,
    val type: InviteType = InviteType.LINK,
    val identifier: String? = null,
    val message: String? = null,
    val expiresInDays: Int? = null
)