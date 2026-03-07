package com.quadro.datasource.repositories.company

import com.quadro.datasource.entities.CompanyMemberEntity
import com.quadro.datasource.entities.CompanyMembersTable
import com.quadro.datasource.mappers.CompanyMemberMapper
import com.quadro.domain.models.company.CompanyMember
import com.quadro.domain.models.company.CompanyRole
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

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

    override suspend fun updateRole(id: UUID, role: CompanyRole): Boolean = newSuspendedTransaction {
        CompanyMemberEntity.findById(id)?.apply {
            this.role = role
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
                    (CompanyMembersTable.role eq role) and
                    (CompanyMembersTable.isActive eq true)
        }.empty()
    }

    override suspend fun getCompanyOwners(companyId: UUID): List<CompanyMember> = newSuspendedTransaction {
        CompanyMemberEntity.find {
            (CompanyMembersTable.companyId eq companyId) and
                    (CompanyMembersTable.role eq CompanyRole.OWNER) and
                    (CompanyMembersTable.isActive eq true)
        }.map { CompanyMemberMapper.toDomain(it) }
    }
}