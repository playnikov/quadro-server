package com.quadro.team.infrastructure.database.mappers

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.team.domain.models.Company
import com.quadro.team.domain.models.CompanyRole
import com.quadro.team.domain.models.CompanyStatus
import com.quadro.team.infrastructure.database.entities.CompanyEntity

object CompanyMapper {
    fun toDomain(entity: CompanyEntity): Company = Company(
        id = entity.id.value,
        companyStatus = CompanyStatus.valueOf(entity.status),
        teamManagementRole = CompanyRole.valueOf(entity.teamManagementRole),
        updatedAt = entity.updatedAt.toKotlinInstant(),
    )

    fun newEntity(domain: Company): CompanyEntity = CompanyEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: CompanyEntity, domain: Company) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: CompanyEntity, domain: Company) {
        entity.status = domain.companyStatus.name
        entity.teamManagementRole = domain.teamManagementRole.name
        entity.updatedAt = domain.updatedAt.toOffsetDateTime()
    }
}