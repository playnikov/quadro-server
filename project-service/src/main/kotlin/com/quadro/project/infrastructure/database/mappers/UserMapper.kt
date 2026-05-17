package com.quadro.project.infrastructure.database.mappers

import com.quadro.project.domain.models.User
import com.quadro.project.domain.models.UserRole
import com.quadro.project.infrastructure.database.entities.UserEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

object UserMapper {
    fun toDomain(entity: UserEntity): User = User(
        id = entity.id.value,
        email = entity.email,
        role = UserRole.valueOf(entity.role),
        isActive = entity.isActive
    )

    fun newEntity(domain: User): UserEntity = UserEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: UserEntity, domain: User) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: UserEntity, domain: User) {
        entity.email = domain.email
        entity.isActive = domain.isActive
        entity.role = domain.role.name
    }
}