package com.quadro.project.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.time.Instant
import java.util.UUID

object CompanyMembersTable : UUIDTable("company_members_copy") {
    val companyId = uuid("company_id").references(CompaniesTable.id)
    val userId = uuid("user_id")
    val role = varchar("role", 50)

    init {
        uniqueIndex(companyId, userId)
    }
}

class CompanyMemberEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CompanyMemberEntity>(CompanyMembersTable)

    var companyId by CompanyMembersTable.companyId
    var userId by CompanyMembersTable.userId
    var role by CompanyMembersTable.role
}