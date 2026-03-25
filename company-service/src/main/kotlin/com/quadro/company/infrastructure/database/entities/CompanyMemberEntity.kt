package com.quadro.company.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.time.Instant
import java.util.UUID

object CompanyMembersTable : UUIDTable("company_members") {
    val companyId = uuid("company_id").references(CompaniesTable.id)
    val userId = uuid("user_id")
    val role = varchar("role", 50)
    val joinedAt = timestampWithTimeZone("joined_at")
    val invitedBy = uuid("invited_by")
    val invitedAt = timestampWithTimeZone("invited_at")
    val lastActiveAt = timestampWithTimeZone("last_active_at").nullable()
    val isActive = bool("is_active").default(true)

    init {
        uniqueIndex(companyId, userId)
    }
}

class CompanyMemberEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CompanyMemberEntity>(CompanyMembersTable)

    var companyId by CompanyMembersTable.companyId
    var userId by CompanyMembersTable.userId
    var role by CompanyMembersTable.role
    var joinedAt by CompanyMembersTable.joinedAt
    var invitedBy by CompanyMembersTable.invitedBy
    var invitedAt by CompanyMembersTable.invitedAt
    var lastActiveAt by CompanyMembersTable.lastActiveAt
    var isActive by CompanyMembersTable.isActive
}