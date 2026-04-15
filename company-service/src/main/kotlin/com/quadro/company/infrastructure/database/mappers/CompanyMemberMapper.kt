package com.quadro.company.infrastructure.database.mappers

import com.quadro.company.domain.models.CompanyMember
import com.quadro.company.domain.models.CompanyRole
import com.quadro.company.infrastructure.database.entities.CompanyMemberEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime

object CompanyMemberMapper {
    fun toDomain(entity: CompanyMemberEntity): CompanyMember = CompanyMember(
        id = entity.id.value,
        companyId = entity.companyId,
        userId = entity.userId,
        role = CompanyRole.valueOf(entity.role),
        joinedAt = entity.joinedAt.toKotlinInstant(),
        invitedBy = entity.invitedBy,
        invitedAt = entity.invitedAt.toKotlinInstant(),
        lastActiveAt = entity.lastActiveAt?.toKotlinInstant()
    )

    fun toEntity(domain: CompanyMember): CompanyMemberEntity =
        CompanyMemberEntity.findById(domain.id) ?: CompanyMemberEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    fun updateEntity(entity: CompanyMemberEntity, domain: CompanyMember) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: CompanyMemberEntity, domain: CompanyMember) {
        entity.companyId = domain.companyId
        entity.userId = domain.userId
        entity.role = domain.role.name
        entity.joinedAt = domain.joinedAt.toOffsetDateTime()
        entity.invitedBy = domain.invitedBy
        entity.invitedAt = domain.invitedAt.toOffsetDateTime()
        entity.lastActiveAt = domain.lastActiveAt?.toOffsetDateTime()
    }
}