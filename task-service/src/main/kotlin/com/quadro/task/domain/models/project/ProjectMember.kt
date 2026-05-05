package com.quadro.task.domain.models.project

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class ProjectRole {
    GUEST, MEMBER, MANAGER, ADMIN, OWNER;

    fun isAtLeast(other: ProjectRole) = ordinal >= other.ordinal
    fun isHigherThan(other: ProjectRole) = ordinal > other.ordinal
}

data class ProjectMember(
    val projectId: UUID,
    val userId: UUID,
    val role: ProjectRole
)