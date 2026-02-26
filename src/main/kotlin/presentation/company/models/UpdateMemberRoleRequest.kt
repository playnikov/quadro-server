package com.quadro.presentation.company.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateMemberRoleRequest(
    val role: String
)
