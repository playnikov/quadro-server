package com.quadro.project.infrastructure.database.mappers

import com.quadro.project.domain.models.Company
import com.quadro.project.domain.models.CompanyRole
import com.quadro.project.domain.models.CompanyStatus
import com.quadro.project.infrastructure.database.entities.CompanyEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Instant

object CompanyMapper {
    fun toDomain(entity: CompanyEntity): Company = Company(
        id = entity.id.value,
        name = entity.name,
        companyStatus = CompanyStatus.valueOf(entity.status),
        updatedAt = entity.updatedAt.toKotlinInstant(),
        projectManagementRole = CompanyRole.valueOf(entity.projectManagementRole),
        currentProjects = entity.currentProjects,
        maxProjects = entity.maxProjects
    )

    fun newEntity(domain: Company): CompanyEntity = CompanyEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: CompanyEntity, domain: Company) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: CompanyEntity, domain: Company) {
        entity.name = domain.name
        entity.status = domain.companyStatus.name
        entity.projectManagementRole = domain.projectManagementRole.name
        entity.currentProjects = domain.currentProjects
        entity.maxProjects = domain.maxProjects
        entity.updatedAt = domain.updatedAt.toOffsetDateTime()
    }
}