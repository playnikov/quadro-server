package com.quadro.team.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object TeamProjectsTable : UUIDTable("team_projects") {
    val teamId = uuid("team_id").references(TeamsTable.id)
    val projectId = uuid("project_id")
    val role = varchar("role", 50)
    val boundAt = timestampWithTimeZone("bound_at")
    val boundBy = uuid("bound_by")
}

class TeamProjectsEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TeamProjectsEntity>(TeamProjectsTable)

    var teamId by TeamProjectsTable.teamId
    var projectId by TeamProjectsTable.projectId
    var role by TeamProjectsTable.role
    var boundAt by TeamProjectsTable.boundAt
    var boundBy by TeamProjectsTable.boundBy
}