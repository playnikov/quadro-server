package com.quadro.company.presentation.models

import com.quadro.company.domain.models.CompanyRole
import com.quadro.company.domain.models.InvitationType
import kotlinx.serialization.Serializable

@Serializable
data class CreateInvitationRequest(
    val teamId: String? = null,
    val role: CompanyRole = CompanyRole.MEMBER,
    val type: InvitationType = InvitationType.LINK,
    val identifier: String? = null,
    val message: String? = null,
    val expiresInDays: Int? = null
)