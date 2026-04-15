package com.quadro.team.infrastructure.database.repositories

import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.team.domain.models.CompanyMember
import com.quadro.team.domain.repositories.CompanyMemberRepository
import com.quadro.team.infrastructure.database.entities.CompanyMemberEntity
import com.quadro.team.infrastructure.database.entities.CompanyMembersTable
import com.quadro.team.infrastructure.database.mappers.CompanyMemberMapper
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