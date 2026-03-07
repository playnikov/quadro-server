package com.quadro.presentation.company.models

import com.quadro.domain.models.company.InvitationResult
import kotlinx.serialization.Serializable

@Serializable
data class InvitationResponse(
    val id: String,
    val companyId: String,
    val companyName: String,
    val teamId: String?,
    val teamName: String?,
    val invitedBy: String,
    val invitedByEmail: String,
    val invitedByName: String,
    val role: String,
    val status: String,
    val token: String,
    val expiresAt: String,
    val createdAt: String,
    val message: String?,
    val inviteLink: String
) {
    companion object {
        private fun formatTime(timestamp: Long): String {
            return java.time.Instant.ofEpochMilli(timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        }

        fun fromInvitationResult(result: InvitationResult): InvitationResponse = InvitationResponse(
            id = result.id.toString(),
            companyId = result.companyId.toString(),
            companyName = result.companyName,
            teamId = result.teamId?.toString(),
            teamName = result.teamName,
            invitedBy = result.invitedBy.toString(),
            invitedByEmail = result.invitedByEmail,
            invitedByName = result.invitedByName,
            role = result.role.toString(),
            status = result.status.toString(),
            token = result.token ?: "",
            expiresAt = formatTime(result.expiresAt),
            createdAt = formatTime(result.createdAt),
            message = result.message,
            inviteLink = result.inviteLink
        )
    }
}
