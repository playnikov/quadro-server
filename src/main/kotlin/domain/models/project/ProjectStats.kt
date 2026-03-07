package com.quadro.domain.models.project

data class ProjectStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val todoTasks: Int = 0,
    val overdueTasks: Int = 0,
    val totalMembers: Int = 0,
    val totalTeams: Int = 0,
    val totalComments: Int = 0,
    val totalAttachments: Int = 0,
    val lastActivityAt: Long? = null
)