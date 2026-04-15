package com.quadro.team.infrastructure.database.mappers

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.team.domain.models.Company
import com.quadro.team.domain.models.CompanyRole
import com.quadro.team.domain.models.CompanyStatus
import com.quadro.team.infrastructure.database.entities.CompanyEntity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Instant

object CompanyMapper {
    fun toDomain(entity: CompanyEntity): Company = Company(
        id = entity.id.value,
        name = entity.name,
        companyStatus = CompanyStatus.valueOf(entity.status),
        createRole = CompanyRole.valueOf(entity.createRole),
        updatedAt = entity.updatedAt.toKotlinInstant(),
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
        entity.createRole = domain.createRole.name
        entity.updatedAt = domain.updatedAt.toOffsetDateTime()
    }
}