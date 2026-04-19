package com.quadro.team.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object CompaniesTable : UUIDTable("companies_copy") {
    val status = varchar("status", 50)
    val teamManagementRole = varchar("manage_team", 50)
    val updatedAt = timestampWithTimeZone("updated_at")
}

class CompanyEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CompanyEntity>(CompaniesTable)

    var status by CompaniesTable.status
    var teamManagementRole by CompaniesTable.teamManagementRole
    var updatedAt by CompaniesTable.updatedAt
}