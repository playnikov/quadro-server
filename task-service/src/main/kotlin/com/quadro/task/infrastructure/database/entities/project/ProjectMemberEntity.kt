package com.quadro.task.infrastructure.database.entities.project

import com.quadro.task.domain.models.project.MemberRole
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.postgresql.util.PGobject
import java.util.UUID

object ProjectMembersTable : UUIDTable("project_members_copy") {
    val projectId = uuid("project_id")
    val userId = uuid("user_id")
    val role = customEnumeration(
        name = "role",
        sql = "member_roles",
        fromDb = { value -> MemberRole.valueOf(value as String) },
        toDb = { role ->
            PGobject().apply {
                type = "member_roles"
                value = role.name
            }
        }
    )
}

class ProjectMemberEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectMemberEntity>(ProjectMembersTable)

    var projectId by ProjectMembersTable.projectId
    var userId by ProjectMembersTable.userId
    var role by ProjectMembersTable.role
}
