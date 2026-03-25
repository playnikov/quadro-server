package com.quadro.company.infrastructure.database.mappers

import com.quadro.company.domain.models.Company
import com.quadro.company.domain.models.CompanyStatus
import com.quadro.company.infrastructure.database.DatabaseFactory
import com.quadro.company.infrastructure.database.entities.CompanyEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Instant

object CompanyMapper {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    fun toDomain(entity: CompanyEntity): Company = Company(
        id = entity.id.value,
        name = entity.name,
        description = entity.description,
        logo = entity.logo,
        website = entity.website,
        email = entity.email,
        phone = entity.phone,
        address = entity.address,
        taxId = entity.taxId,
        companyStatus = CompanyStatus.valueOf(entity.status),
        ownerId = entity.ownerId,
        companySettings = json.decodeFromString(entity.settings),
        createdAt = entity.createdAt.toKotlinInstant(),
        updatedAt = entity.updatedAt.toKotlinInstant(),
        deletedAt = entity.deletedAt?.toKotlinInstant(),
        maxUsers = entity.maxUsers,
        maxProjects = entity.maxProjects,
        currentUsers = entity.currentUsers,
        currentProjects = entity.currentProjects
    )

    fun toEntity(domain: Company): CompanyEntity = CompanyEntity.findById(domain.id) ?: CompanyEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: CompanyEntity, domain: Company) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: CompanyEntity, domain: Company) {
        entity.name = domain.name
        entity.description = domain.description
        entity.logo = domain.logo
        entity.website = domain.website
        entity.email = domain.email
        entity.phone = domain.phone
        entity.address = domain.address
        entity.taxId = domain.taxId
        entity.status = domain.companyStatus.name
        entity.ownerId = domain.ownerId
        entity.createdAt = domain.createdAt.toOffsetDateTime()
        entity.updatedAt = domain.updatedAt.toOffsetDateTime()
        entity.deletedAt = domain.deletedAt?.toOffsetDateTime()
        entity.maxUsers = domain.maxUsers
        entity.maxProjects = domain.maxProjects
        entity.currentUsers = domain.currentUsers
        entity.currentProjects = domain.currentProjects

        val settingsJson = json.encodeToString(domain.companySettings)
        logger.debug("Saving settings: $settingsJson")
        entity.settings = settingsJson
    }
}