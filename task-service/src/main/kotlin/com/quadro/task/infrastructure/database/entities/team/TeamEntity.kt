package com.quadro.task.infrastructure.database.entities.team

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object TeamsTable : UUIDTable("teams_copy") {
    val status = varchar("status", 50)
}

class TeamEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TeamEntity>(TeamsTable)

    var status by TeamsTable.status
}