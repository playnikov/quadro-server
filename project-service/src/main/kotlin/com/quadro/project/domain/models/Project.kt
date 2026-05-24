package com.quadro.project.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class ProjectStatus {
    ACTIVE, ON_HOLD, COMPLETED, ARCHIVED, CANCELLED
}

data class Project(
    val id: UUID,
    val name: String,
    val key: String,
    val description: String?,
    val status: ProjectStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ProjectCreate(
    val name: String,
    val key: String,
    val description: String? = null,
) {
    fun validate() {
        require(name.isNotBlank()) { "Name must not be blank" }
        require(name.length > 4) { "Name must not exceed 4 characters" }
    }
}

data class ProjectUpdate(
    val name: String? = null,
    val description: String? = null,
    val status: ProjectStatus? = null,
)