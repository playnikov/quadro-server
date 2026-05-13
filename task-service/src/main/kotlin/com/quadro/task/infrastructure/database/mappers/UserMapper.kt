package com.quadro.task.infrastructure.database.mappers

import com.quadro.task.infrastructure.database.entities.UserEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.User
import com.quadro.task.domain.models.UserRole
import kotlin.time.Instant
import kotlin.time.toJavaInstant

object UserMapper {
    fun toDomain(entity: UserEntity): User = User(
        id = entity.id.value,
        firstName = entity.firstName,
        lastName = entity.lastName,
        isActive = entity.isActive,
        role = UserRole.valueOf(entity.role),
    )

    fun newEntity(domain: User): UserEntity = UserEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: UserEntity, domain: User) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: UserEntity, domain: User) {
        entity.firstName = domain.firstName
        entity.lastName = domain.lastName
        entity.role = domain.role.name
        entity.isActive = domain.isActive
    }
}