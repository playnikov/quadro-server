package com.quadro.team.presentation.models

import kotlinx.serialization.Serializable

@Serializable
data class TeamCreateRequest(
    val name: String,
    val description: String? = null,
    val avatar: String? = null,
    val leadId: String,
    val visibility: String,
    val initialMembers: List<String>? = emptyList()
)

@Serializable
data class TeamUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val avatar: String? = null,
    val leadId: String? = null,
    val visibility: String? = null,
    val status: String? = null
)

@Serializable
data class TeamProjectBinding(
    val teamId: String,
    val projectId: String,
    val role: String
)

@Serializable
data class AddMemberRequest(
    val userId: String,
    val role: String
)

@Serializable
data class UpdateMemberRole(
    val userId: String,
    val role: String
)

@Serializable
data class RemoveMember(
    val userId: String
)