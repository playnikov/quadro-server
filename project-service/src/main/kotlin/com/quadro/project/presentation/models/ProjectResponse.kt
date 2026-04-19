package com.quadro.project.presentation.models

import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectPriority
import com.quadro.project.domain.models.ProjectStatus
import com.quadro.project.domain.models.ProjectType
import com.quadro.project.domain.models.ProjectVisibility
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ProjectResponse(
    val id: String,
    val companyId: String,
    val type: ProjectType,
    val name: String,
    val key: String,
    val description: String?,
    val status: ProjectStatus,
    val priority: ProjectPriority,
    val visibility: ProjectVisibility,
    val leadId: String,
    val ownerId: String,
    val startDate: Instant?,
    val endDate: Instant?,
    val completedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(project: Project): ProjectResponse = ProjectResponse(
            id = project.id.toString(),
            companyId = project.companyId.toString(),
            type = project.type,
            name = project.name,
            key = project.key,
            description = project.description,
            status = project.status,
            priority = project.priority,
            visibility = project.visibility,
            leadId = project.leadId.toString(),
            ownerId = project.ownerId.toString(),
            startDate = project.startDate,
            endDate = project.endDate,
            completedAt = project.completedAt,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt
        )
    }
}