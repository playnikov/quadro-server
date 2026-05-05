package com.quadro.task.infrastructure.database.entities.project

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object ProjectMembersTable : UUIDTable("project_members_copy") {
    val projectId = uuid("project_id")
    val userId = uuid("user_id")
    val role = varchar("role", 50)
}

class ProjectMemberEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectMemberEntity>(ProjectMembersTable)

    var projectId by ProjectMembersTable.projectId
    var userId by ProjectMembersTable.userId
    var role by ProjectMembersTable.role
}
