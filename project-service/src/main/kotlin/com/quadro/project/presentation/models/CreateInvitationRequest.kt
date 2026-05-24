package com.quadro.project.presentation.models

import com.quadro.project.domain.models.InviteType
import com.quadro.project.domain.models.MemberRole
import kotlinx.serialization.Serializable

@Serializable
data class CreateInvitationRequest(
    val role: MemberRole = MemberRole.MEMBER,
    val type: InviteType = InviteType.LINK,
    val identifier: String? = null,
    val message: String? = null,
    val expiresInDays: Int? = null
)