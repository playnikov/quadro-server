package com.quadro.project.infrastructure.database.repositories

import com.quadro.project.domain.models.CompanyMember
import com.quadro.project.domain.repositories.CompanyMemberRepository
import com.quadro.project.infrastructure.database.entities.CompanyMemberEntity
import com.quadro.project.infrastructure.database.entities.CompanyMembersTable
import com.quadro.project.infrastructure.database.mappers.CompanyMemberMapper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class CompanyMemberRepositoryImpl : CompanyMemberRepository {
    override suspend fun upsert(member: CompanyMember): CompanyMember = newSuspendedTransaction {
        val existing = CompanyMemberEntity.findById(member.id)
        val entity = if (existing != null) {
            CompanyMemberMapper.updateEntity(existing, member)
            existing
        } else {
            CompanyMemberMapper.newEntity(member)
        }
        CompanyMemberMapper.toDomain(entity)
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

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        CompanyMemberEntity.findById(id)?.delete() != null
    }

}