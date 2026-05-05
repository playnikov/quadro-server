package com.quadro.task.infrastructure.database.entities.team

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object TeamProjectsTable : UUIDTable("team_projects_copy") {
    val teamId = uuid("team_id").references(TeamsTable.id)
    val projectId = uuid("project_id")
    val role = varchar("role", 50)
}

class TeamProjectsEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TeamProjectsEntity>(TeamProjectsTable)

    var teamId by TeamProjectsTable.teamId
    var projectId by TeamProjectsTable.projectId
    var role by TeamProjectsTable.role
}