package com.quadro.datasource.repositories.company

import com.quadro.datasource.entities.CompaniesTable
import com.quadro.datasource.entities.CompanyEntity
import com.quadro.datasource.entities.CompanyMembersTable
import com.quadro.datasource.mappers.CompanyMapper
import com.quadro.domain.models.company.Company
import com.quadro.domain.models.company.CompanyStatus
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

class CompanyRepositoryImpl : CompanyRepository {
    override suspend fun create(company: Company): Company = newSuspendedTransaction {
        CompanyMapper.toDomain(CompanyMapper.toEntity(company))
    }

    override suspend fun findById(id: UUID): Company? = newSuspendedTransaction {
        CompanyEntity.findById(id)?.let { CompanyMapper.toDomain(it) }
    }

    override suspend fun findByName(name: String): Company? = newSuspendedTransaction {
        CompanyEntity.find { CompaniesTable.name eq name }
            .firstOrNull()
            ?.let { CompanyMapper.toDomain(it) }
    }

    override suspend fun update(company: Company): Company = newSuspendedTransaction {
        val entity = CompanyEntity.findById(company.id)
            ?: throw IllegalArgumentException("Company not found with id: ${company.id}")
        CompanyMapper.updateEntity(entity, company)
        CompanyMapper.toDomain(entity)
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        CompanyEntity.findById(id)?.delete() != null
    }

    override suspend fun findByOwner(
        ownerId: UUID,
        limit: Int,
        offset: Int
    ): List<Company>  = newSuspendedTransaction {
        CompanyEntity.find { CompaniesTable.ownerId eq ownerId }
            .limit(limit).offset(offset.toLong())
            .orderBy(CompaniesTable.createdAt to SortOrder.DESC)
            .map { CompanyMapper.toDomain(it) }
    }

    override suspend fun findByUser(
        userId: UUID,
        limit: Int,
        offset: Int
    ): List<Company> = newSuspendedTransaction {
        CompanyEntity.wrapRows(
            CompaniesTable
                .join(
                    CompanyMembersTable, JoinType.INNER,
                    additionalConstraint = { CompaniesTable.id eq CompanyMembersTable.companyId })
                .selectAll()
                .where { CompanyMembersTable.userId eq userId }
                .orderBy(CompaniesTable.createdAt to SortOrder.DESC)
                .withDistinct()
                .limit(limit).offset(offset.toLong())
        ).toList().map { CompanyMapper.toDomain(it) }
    }

    override suspend fun countByOwner(ownerId: UUID): Long = newSuspendedTransaction {
        CompanyEntity.find { CompaniesTable.ownerId eq ownerId }.count()
    }

    override suspend fun countByUser(userId: UUID): Long = newSuspendedTransaction{
        CompanyEntity.wrapRows(
            CompaniesTable
                .join(
                    CompanyMembersTable, JoinType.INNER,
                    additionalConstraint = { CompaniesTable.id eq CompanyMembersTable.companyId })
                .selectAll()
                .where { CompanyMembersTable.userId eq userId }
                .orderBy(CompaniesTable.createdAt to SortOrder.DESC)
                .withDistinct()
        ).count()
    }

    override suspend fun existsByName(name: String): Boolean = newSuspendedTransaction {
        !CompanyEntity.find { CompaniesTable.name eq name }.empty()
    }

    override suspend fun updateStatus(
        id: UUID,
        status: CompanyStatus
    ): Boolean = newSuspendedTransaction {
        CompanyEntity.findById(id)?.apply {
            this.status = status
            updatedAt = Instant.now()
        } != null
    }
}