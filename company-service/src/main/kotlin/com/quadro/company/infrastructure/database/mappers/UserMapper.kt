package com.quadro.company.infrastructure.database.mappers

import com.quadro.company.domain.models.User
import com.quadro.company.infrastructure.database.entities.UserEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

object UserMapper {
    fun toDomain(entity: UserEntity): User = User(
        id = entity.id.value,
        email = entity.email,
        firstName = entity.firstName,
        lastName = entity.lastName,
        middleName = entity.middleName,
        avatar = entity.avatar,
        isActive = entity.isActive,
    )

    fun newEntity(domain: User): UserEntity = UserEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: UserEntity, domain: User) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: UserEntity, domain: User) {
        entity.email = domain.email
        entity.firstName = domain.firstName
        entity.lastName = domain.lastName
        entity.middleName = domain.middleName
        entity.avatar = domain.avatar
        entity.isActive = domain.isActive
    }
}