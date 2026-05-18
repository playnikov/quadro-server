package com.quadro.notification.domain.models.project

import java.util.UUID

data class Project(
    val id: UUID,
    val key: String,
    val status: ProjectStatus
)

enum class ProjectStatus {
    ACTIVE, ARCHIVED, DELETED
}