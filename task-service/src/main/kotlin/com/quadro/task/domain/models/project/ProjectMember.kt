package com.quadro.task.domain.models.project

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class MemberRole {
    GUEST, MEMBER, MANAGER, ADMIN, OWNER;

    fun isAtLeast(other: MemberRole) = ordinal >= other.ordinal
    fun isHigherThan(other: MemberRole) = ordinal > other.ordinal
}

data class ProjectMember(
    val projectId: UUID,
    val userId: UUID,
    val role: MemberRole
)