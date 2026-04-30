package com.quadro.team.infrastructure.database.mappers

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.team.domain.models.User
import com.quadro.team.domain.models.UserRole
import com.quadro.team.infrastructure.database.entities.UserEntity
import kotlin.time.Instant
import kotlin.time.toJavaInstant

object UserMapper {
    fun toDomain(entity: UserEntity): User = User(
        id = entity.id.value,
        role = UserRole.valueOf(entity.role),
        isActive = entity.isActive,
    )

    fun newEntity(domain: User): UserEntity = UserEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: UserEntity, domain: User) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: UserEntity, domain: User) {
        entity.role = domain.role.name
        entity.isActive = domain.isActive
    }
}