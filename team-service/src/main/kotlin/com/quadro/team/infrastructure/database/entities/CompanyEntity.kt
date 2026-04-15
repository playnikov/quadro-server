package com.quadro.team.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object CompaniesTable : UUIDTable("companies_copy") {
    val name = varchar("name", 255).uniqueIndex()
    val status = varchar("status", 50)
    val createRole = varchar("create_role", 50)
    val updatedAt = timestampWithTimeZone("updated_at")
}

class CompanyEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CompanyEntity>(CompaniesTable)

    var name by CompaniesTable.name
    var status by CompaniesTable.status
    var createRole by CompaniesTable.createRole
    var updatedAt by CompaniesTable.updatedAt
}