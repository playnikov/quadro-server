package com.quadro.project.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object ProjectMembersTable : UUIDTable("project_members") {
    val projectId = uuid("project_id").references(ProjectsTable.id)
    val userId = uuid("user_id")
    val role = varchar("role", 50)
    val joinedAt = timestampWithTimeZone("joined_at")
    val invitedBy = uuid("invited_by")
    val invitedAt = timestampWithTimeZone("invited_at")
}

class ProjectMemberEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectMemberEntity>(ProjectMembersTable)

    var projectId by ProjectMembersTable.projectId
    var userId by ProjectMembersTable.userId
    var role by ProjectMembersTable.role
    var joinedAt by ProjectMembersTable.joinedAt
    var invitedBy by ProjectMembersTable.invitedBy
    var invitedAt by ProjectMembersTable.invitedAt
}