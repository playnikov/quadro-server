package com.quadro.team.infrastructure.database.repositories

import com.quadro.team.domain.models.Company
import com.quadro.team.domain.repositories.CompanyRepository
import com.quadro.team.infrastructure.database.entities.CompanyEntity
import com.quadro.team.infrastructure.database.mappers.CompanyMapper
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class CompanyRepositoryImpl : CompanyRepository {
    override suspend fun upsert(company: Company): Company = newSuspendedTransaction {
        val existing = CompanyEntity.findById(company.id)
        val entity = if (existing != null) {
            CompanyMapper.updateEntity(existing, company)
            existing
        } else {
            CompanyMapper.newEntity(company)
        }
        CompanyMapper.toDomain(entity)
    }

    override suspend fun findById(id: UUID): Company? = newSuspendedTransaction {
        CompanyEntity.findById(id)?.let { CompanyMapper.toDomain(it) }
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        CompanyEntity.findById(id)?.delete() != null
    }

}