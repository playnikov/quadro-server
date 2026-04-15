package com.quadro.team.infrastructure.database.mappers

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.team.domain.models.CompanyMember
import com.quadro.team.domain.models.CompanyRole
import com.quadro.team.infrastructure.database.entities.CompanyMemberEntity

object CompanyMemberMapper {
    fun toDomain(entity: CompanyMemberEntity): CompanyMember = CompanyMember(
        id = entity.id.value,
        companyId = entity.companyId,
        userId = entity.userId,
        role = CompanyRole.valueOf(entity.role)
    )

    fun newEntity(domain: CompanyMember): CompanyMemberEntity = CompanyMemberEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: CompanyMemberEntity, domain: CompanyMember) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: CompanyMemberEntity, domain: CompanyMember) {
        entity.companyId = domain.companyId
        entity.userId = domain.userId
        entity.role = domain.role.name
    }
}