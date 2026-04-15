package com.quadro.company.infrastructure.database.repositories

import com.quadro.company.domain.models.CompanyMember
import com.quadro.company.domain.models.CompanyRole
import com.quadro.company.domain.repositories.CompanyMemberRepository
import com.quadro.company.infrastructure.database.entities.CompaniesTable
import com.quadro.company.infrastructure.database.entities.CompanyEntity
import com.quadro.company.infrastructure.database.entities.CompanyMemberEntity
import com.quadro.company.infrastructure.database.entities.CompanyMembersTable
import com.quadro.company.infrastructure.database.mappers.CompanyMemberMapper
import com.quadro.shared.utils.toOffsetDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

class CompanyMemberRepositoryImpl : CompanyMemberRepository {
    override suspend fun findById(id: UUID): CompanyMember? = newSuspendedTransaction {
        CompanyMemberEntity.findById(id)?.let { CompanyMemberMapper.toDomain(it) }
    }

    override suspend fun findByCompanyAndUser(
        companyId: UUID,
        userId: UUID
    ): CompanyMember? = newSuspendedTransaction {
        CompanyMemberEntity.find {
            (CompanyMembersTable.companyId eq companyId) and
                    (CompanyMembersTable.userId eq userId)
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

    override suspend fun findByCompanyAndRole(
        companyId: UUID,
        role: CompanyRole
    ): List<CompanyMember> = newSuspendedTransaction {
        CompanyMemberEntity.find {
            (CompanyMembersTable.companyId eq companyId) and
                    (CompanyMembersTable.role eq role.name)
        }.map { CompanyMemberMapper.toDomain(it) }
    }

    override suspend fun exists(companyId: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        CompanyMemberEntity.find {
            (CompanyMembersTable.companyId eq companyId) and
                    (CompanyMembersTable.userId eq userId)
        }.count() > 0
    }

    override suspend fun add(member: CompanyMember): CompanyMember = newSuspendedTransaction {
        val entity = CompanyMemberMapper.toEntity(member)
        CompanyMemberMapper.toDomain(entity)
    }

    override suspend fun remove(id: UUID): Unit = newSuspendedTransaction {
        CompanyMemberEntity.findById(id)?.delete()
    }

    override suspend fun updateRole(id: UUID, role: CompanyRole): Unit = newSuspendedTransaction {
        CompanyMemberEntity.findById(id)?.apply {
            this.role = role.name
        } != null
    }

    override suspend fun updateLastActive(companyId: UUID, userId: UUID) {
        CompanyMembersTable.update({
            (CompanyMembersTable.companyId eq companyId) and
                    (CompanyMembersTable.userId eq userId)
        }) {
            it[CompanyMembersTable.lastActiveAt] = Clock.System.now().toOffsetDateTime()
        }
    }

    override suspend fun countByCompany(companyId: UUID): Long = newSuspendedTransaction {
        CompanyMembersTable.selectAll()
            .where { CompanyMembersTable.companyId eq companyId }
            .count()
    }

    override suspend fun findNewMembers(
        companyId: UUID,
        since: Instant
    ): List<CompanyMember> = newSuspendedTransaction {
        CompanyMemberEntity.find {
            (CompanyMembersTable.companyId eq companyId) and
                    (CompanyMembersTable.joinedAt greaterEq since.toOffsetDateTime())
        }.map { CompanyMemberMapper.toDomain(it) }
    }
}