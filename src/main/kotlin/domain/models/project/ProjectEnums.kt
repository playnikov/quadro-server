package com.quadro.domain.models.project

enum class ProjectType {
    TEAM_MANAGED, COMPANY_MANAGED
}

enum class ProjectStatus {
    ACTIVE, ON_HOLD, COMPLETED, ARCHIVED, CANCELLED
}

enum class ProjectPriority {
    HIGHEST, HIGH, MEDIUM, LOW, LOWEST
}

enum class ProjectVisibility {
    PUBLIC, RESTRICTED, PRIVATE
}

enum class ProjectRole {
    OWNER, LEAD, ADMIN, MEMBER, VIEWER
}