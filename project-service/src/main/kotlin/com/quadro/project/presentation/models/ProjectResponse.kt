package com.quadro.project.presentation.models

import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectStatus
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ProjectResponse(
    val id: String,
    val name: String,
    val key: String,
    val description: String?,
    val status: ProjectStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(project: Project): ProjectResponse = ProjectResponse(
            id = project.id.toString(),
            name = project.name,
            key = project.key,
            description = project.description,
            status = project.status,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt
        )
    }
}