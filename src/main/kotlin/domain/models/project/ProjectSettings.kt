package com.quadro.domain.models.project

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ProjectSettings(
    val taskPrefix: String? = null,
    val defaultTaskPriority: String = "MEDIUM",
    val defaultAssignee: String? = null,
    val allowGuestAccess: Boolean = false,
    val requireTaskApproval: Boolean = false,
    val requireTimeTracking: Boolean = false,
    val enableSubtasks: Boolean = true,
    val maxSubtasksPerTask: Int = 10,
    val enableWatchers: Boolean = true,
    val enableComments: Boolean = true,
    val enableAttachments: Boolean = true,
    val maxAttachmentSize: Long = 10485760,
    val allowedAttachmentTypes: List<String> = listOf("*/*")
)