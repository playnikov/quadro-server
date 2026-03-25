package com.quadro.company.infrastructure.database.repositories

import com.quadro.company.domain.models.CompanyMember
import com.quadro.company.domain.models.CompanyRole
import com.quadro.company.domain.repositories.CompanyMemberRepository
import com.quadro.company.infrastructure.database.entities.CompanyMemberEntity
import com.quadro.company.infrastructure.database.entities.CompanyMembersTable
import com.quadro.company.infrastructure.database.mappers.CompanyMemberMapper
import com.quadro.shared.utils.toOffsetDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.time.Clock

class CompanyMemberRepositoryImpl : CompanyMemberRepository {
    override suspend fun add(member: CompanyMember): CompanyMember = newSuspendedTransaction {
        val entity = CompanyMemberMapper.toEntity(member)
        CompanyMemberMapper.toDomain(entity)
    }

    override suspend fun findById(id: UUID): CompanyMember? = newSuspendedTransaction {
        CompanyMemberEntity.findById(id)?.let { CompanyMemberMapper.toDomain(it) }
    }

    override suspend fun findByCompanyAndUser(
        companyId: UUID,
        userId: UUID
    ): CompanyMember? = newSuspendedTransaction {
        CompanyMemberEntity.find {
            (CompanyMembersTable.companyId eq companyId) and
                    (CompanyMembersTable.userId eq userId) and
                    (CompanyMembersTable.isActive eq true)
        }.firstOrNull()?.let { CompanyMemberMapper.toDomain(it) }
    }

    override suspend fun findByCompany(
        companyId: UUID,
        limit: Int,
        offset: Int
    ): List<CompanyMember> = newSuspendedTransaction {
        CompanyMemberEntity.find { CompanyMembersTable.companyId eq companyId }
            .limit(limit).offset(offset.toLong())
            .map { CompanyMemberMapper.toDomain(it) }
    }

    override suspend fun findByUser(
        userId: UUID,
        limit: Int,
        offset: Int
    ): List<CompanyMember> = newSuspendedTransaction {
        CompanyMemberEntity.find { CompanyMembersTable.userId eq userId }
            .limit(limit).offset(offset.toLong())
            .map { CompanyMemberMapper.toDomain(it) }
    }

    override suspend fun updateRole(
        id: UUID,
        role: CompanyRole
    ): Boolean = newSuspendedTransaction {
        CompanyMemberEntity.findById(id)?.apply {
            this.role = role.name
        } != null
    }

    override suspend fun updateLastActive(id: UUID): Boolean = newSuspendedTransaction {
        CompanyMemberEntity.findById(id)?.apply {
            this.lastActiveAt = Clock.System.now().toOffsetDateTime()
        } != null
    }

    override suspend fun remove(id: UUID): Boolean = newSuspendedTransaction {
        CompanyMemberEntity.findById(id)?.delete() != null
    }

    override suspend fun removeByCompanyAndUser(companyId: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        val member = findByCompanyAndUser(companyId, userId)
        if (member != null) {
            CompanyMemberEntity.findById(member.id)?.delete() != null
        } else false
    }

    override suspend fun countByCompany(companyId: UUID): Long = newSuspendedTransaction {
        CompanyMemberEntity.find { CompanyMembersTable.companyId eq companyId }.count()
    }

    override suspend fun countByUser(userId: UUID): Long = newSuspendedTransaction {
        CompanyMemberEntity.find { CompanyMembersTable.userId eq userId }.count()
    }

    override suspend fun exists(companyId: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        !CompanyMemberEntity.find {
            (CompanyMembersTable.companyId eq companyId) and
                    (CompanyMembersTable.userId eq userId) and
                    (CompanyMembersTable.isActive eq true)
        }.empty()
    }

    override suspend fun isUserInRole(
        companyId: UUID,
        userId: UUID,
        role: CompanyRole
    ): Boolean = newSuspendedTransaction {
        !CompanyMemberEntity.find {
            (CompanyMembersTable.companyId eq companyId) and
                    (CompanyMembersTable.userId eq userId) and
                    (CompanyMembersTable.role eq role.name) and
                    (CompanyMembersTable.isActive eq true)
        }.empty()
    }
}