package com.quadro.data.models

import java.time.LocalDateTime

data class ProjectModel(
    val id: Long,
    val title: String,
    val description: String?,
    val createdBy: Long,
    val status: ProjectStatus,
    val type: ProjectType,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class ProjectStatus {
    ACTIVE, ARCHIVED, COMPLETED
}

enum class ProjectType {
    INTERNAL, GIT_LAB, GIT_HUB
}