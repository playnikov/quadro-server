package com.quadro.shared.data.messaging

object KafkaTopics {
    // User events
    const val USER_CREATED = "quadro.user.created"
    const val USER_UPDATED = "quadro.user.updated"
    const val USER_DEACTIVATED = "quadro.user.deactivated"

    // Project events
    const val PROJECT_CREATED = "quadro.project.created"
    const val PROJECT_UPDATED = "quadro.project.updated"
    const val PROJECT_ARCHIVED = "quadro.project.archived"
    const val PROJECT_DELETED = "quadro.project.deleted"
    const val PROJECT_MEMBER_ADDED = "quadro.project.member.added"
    const val PROJECT_MEMBER_INVITED = "quadro.project.member.invited"
    const val PROJECT_MEMBER_REMOVED = "quadro.project.member.removed"
    const val PROJECT_MEMBER_ROLE_UPDATED = "quadro.project.member.role.updated"

    // Team events
    const val TEAM_CREATED = "quadro.team.created"
    const val TEAM_UPDATED = "quadro.team.updated"
    const val TEAM_DELETED = "quadro.team.deleted"
    const val TEAM_MEMBER_ADDED = "quadro.team.member.added"
    const val TEAM_MEMBER_UPDATED = "quadro.team.member.updated"
    const val TEAM_MEMBER_REMOVED = "quadro.team.member.removed"
    const val TEAM_PROJECT_ASSIGNED = "quadro.team.project.assigned"
    const val TEAM_PROJECT_UPDATED = "quadro.team.project.updated"
    const val TEAM_PROJECT_UNASSIGNED = "quadro.team.project.unassigned"

    // Task events
    const val TASK_CREATED = "quadro.task.created"
    const val TASK_UPDATED = "quadro.task.updated"
    const val TASK_ASSIGNED = "quadro.task.assigned"
    const val TASK_COMMENT = "quadro.task.comment"
    const val TASK_STATUS_CHANGED = "quadro.task.status.changed"
    const val TASK_COMPLETED = "quadro.task.completed"
    const val TASK_DELETED = "quadro.task.deleted"

    // Stats
    const val STATS_SNAPSHOT_REQUESTED = "quadro.stats.snapshot.requested"
    const val STATS_UPDATED = "quadro.stats.updated"
}