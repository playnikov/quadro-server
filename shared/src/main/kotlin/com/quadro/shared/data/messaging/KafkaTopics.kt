package com.quadro.shared.data.messaging

object KafkaTopics {
    // User events
    const val USER_CREATED = "quadro.user.created"
    const val USER_UPDATED = "quadro.user.updated"
    const val USER_DEACTIVATED = "quadro.user.deactivated"

    // Company events
    const val COMPANY_CREATED = "quadro.company.created"
    const val COMPANY_UPDATED = "quadro.company.updated"
    const val COMPANY_DELETED = "quadro.company.deleted"
    const val COMPANY_MEMBER_ADDED = "quadro.company.member.added"
    const val COMPANY_MEMBER_REMOVED = "quadro.company.member.removed"
    const val COMPANY_MEMBER_ROLE_UPDATED = "quadro.company.member.role.updated"

    // Team events
    const val TEAM_CREATED = "quadro.team.created"
    const val TEAM_MEMBER_ADDED = "quadro.team.member.added"
    const val TEAM_MEMBER_REMOVED = "quadro.team.member.removed"
    const val TEAM_DELETED = "quadro.team.deleted"

    // Project events
    const val PROJECT_CREATED = "quadro.project.created"
    const val PROJECT_UPDATED = "quadro.project.updated"
    const val PROJECT_ARCHIVED = "quadro.project.archived"
    const val PROJECT_DELETED = "quadro.project.deleted"
    const val PROJECT_MEMBER_ADDED = "quadro.project.member.added"
    const val PROJECT_TEAM_ASSIGNED = "quadro.project.team.assigned"

    // Task events
    const val TASK_CREATED = "quadro.task.created"
    const val TASK_UPDATED = "quadro.task.updated"
    const val TASK_ASSIGNED = "quadro.task.assigned"
    const val TASK_STATUS_CHANGED = "quadro.task.status.changed"
    const val TASK_COMPLETED = "quadro.task.completed"
    const val TASK_DELETED = "quadro.task.deleted"

    // Stats
    const val STATS_SNAPSHOT_REQUESTED = "quadro.stats.snapshot.requested"
    const val STATS_UPDATED = "quadro.stats.updated"
}