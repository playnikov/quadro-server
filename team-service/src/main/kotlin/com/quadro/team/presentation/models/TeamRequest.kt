package com.quadro.team.presentation.models

import kotlinx.serialization.Serializable

@Serializable
data class TeamCreateRequest(
    val name: String,
    val description: String?,
    val avatar: String?,
    val leadId: String?,
    val visibility: String,
    val initialMembers: List<String>?
)

@Serializable
data class TeamUpdateRequest(
    val name: String?,
    val description: String?,
    val avatar: String?,
    val leadId: String?,
    val visibility: String?,
    val status: String?
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