package com.quadro.team.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object CompaniesTable : UUIDTable("companies") {
    val name = varchar("name", 255).uniqueIndex()
    val description = text("description").nullable()
    val logo = varchar("logo", 500).nullable()
    val website = varchar("website", 255).nullable()
    val email = varchar("email", 255).nullable()
    val phone = varchar("phone", 50).nullable()
    val address = text("address").nullable()
    val taxId = varchar("tax_id", 50).nullable()
    val status = varchar("status", 50)
    val ownerId = uuid("owner_id")
    val settings = text("settings")
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
    val deletedAt = timestampWithTimeZone("deleted_at").nullable()
    val maxUsers = integer("max_users")
    val currentUsers = integer("current_users")
    val maxProjects = integer("max_projects")
    val currentProjects = integer("current_projects")
}

class CompanyEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CompanyEntity>(CompaniesTable)

    var name by CompaniesTable.name
    var description by CompaniesTable.description
    var logo by CompaniesTable.logo
    var website by CompaniesTable.website
    var email by CompaniesTable.email
    var phone by CompaniesTable.phone
    var address by CompaniesTable.address
    var taxId by CompaniesTable.taxId
    var status by CompaniesTable.status
    var ownerId by CompaniesTable.ownerId
    var settings by CompaniesTable.settings
    var createdAt by CompaniesTable.createdAt
    var updatedAt by CompaniesTable.updatedAt
    var deletedAt by CompaniesTable.deletedAt
    var maxUsers by CompaniesTable.maxUsers
    var currentUsers by CompaniesTable.currentUsers
    var maxProjects by CompaniesTable.maxProjects
    var currentProjects by CompaniesTable.currentProjects

}