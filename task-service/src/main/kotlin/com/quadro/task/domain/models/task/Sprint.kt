package com.quadro.task.domain.models.task

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class SprintStatus { PLANNING, ACTIVE, COMPLETED, CANCELLED }


data class Sprint(
    val id: UUID,
    val projectId: UUID,
    val name: String,
    val goal: String?,
    val status: SprintStatus,
    val startDate: Instant,
    val endDate: Instant,
    val createdBy: UUID,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class SprintCreate(
    val projectId: UUID,
    val name: String,
    val goal: String?,
    val status: SprintStatus,
    val startDate: Instant,
    val endDate: Instant,
    val createdBy: UUID
) {
    fun validate() {
        require(endDate > startDate) { "endDate must be greater than startDate" }
    }
}

data class SprintUpdate(
    val name: String? = null,
    val goal: String? = null,
    val status: SprintStatus? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null
) {
    fun validate() {
        if (startDate != null && endDate != null) {
            require(endDate > startDate) { "endDate must be greater than startDate" }
        }
    }
}