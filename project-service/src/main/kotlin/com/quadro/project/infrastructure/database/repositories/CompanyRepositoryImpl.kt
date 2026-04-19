package com.quadro.project.infrastructure.database.repositories

import com.quadro.project.domain.models.Company
import com.quadro.project.domain.repositories.CompanyRepository
import com.quadro.project.infrastructure.database.entities.CompanyEntity
import com.quadro.project.infrastructure.database.mappers.CompanyMapper
import com.quadro.shared.utils.toOffsetDateTime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.time.Clock

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

    override suspend fun incrementProjectCount(id: UUID): Boolean = newSuspendedTransaction {
        CompanyEntity.findById(id)?.apply {
            currentProjects += 1
            updatedAt = Clock.System.now().toOffsetDateTime()
        } != null
    }

    override suspend fun decrementProjectCount(id: UUID): Boolean = newSuspendedTransaction {
        CompanyEntity.findById(id)?.apply {
            currentProjects -= 1
            updatedAt = Clock.System.now().toOffsetDateTime()
        } != null
    }
}