package com.quadro.project.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object CompaniesTable : UUIDTable("companies_copy") {
    val name = varchar("name", 255).uniqueIndex()
    val status = varchar("status", 50)
    val projectManagementRole = varchar("project_management_role", 50)
    val maxProjects = integer("max_projects")
    val currentProjects = integer("current_projects")
    val updatedAt = timestampWithTimeZone("updated_at")
}

class CompanyEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CompanyEntity>(CompaniesTable)

    var name by CompaniesTable.name
    var status by CompaniesTable.status
    var projectManagementRole by CompaniesTable.projectManagementRole
    var maxProjects by CompaniesTable.maxProjects
    var currentProjects by CompaniesTable.currentProjects
    var updatedAt by CompaniesTable.updatedAt
}