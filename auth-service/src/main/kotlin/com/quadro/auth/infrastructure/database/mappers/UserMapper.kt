package com.quadro.auth.infrastructure.database.mappers

import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.infrastructure.database.entities.UserEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

object UserMapper {
    fun toDomain(entity: UserEntity): User = User(
        id = entity.id.value,
        username = entity.username,
        email = entity.email,
        passwordHash = entity.passwordHash,
        firstName = entity.firstName,
        lastName = entity.lastName,
        middleName = entity.middleName,
        avatarUrl = entity.avatar,
        role = UserRole.valueOf(entity.role),
        isActive = entity.isActive,
        isEmailVerified = entity.isEmailVerified,
        createdAt = entity.createdAt.toKotlinInstant(),
        updatedAt = entity.updatedAt.toKotlinInstant(),
        lastLoginAt = entity.lastLoginAt?.toKotlinInstant(),
        lastLoginIp = entity.lastLoginIp
    )

    fun toEntity(domain: User): UserEntity = UserEntity.findById(domain.id) ?: UserEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: UserEntity, domain: User) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: UserEntity, domain: User) {
        entity.username = domain.username
        entity.email = domain.email
        entity.passwordHash = domain.passwordHash
        entity.firstName = domain.firstName
        entity.lastName = domain.lastName
        entity.middleName = domain.middleName
        entity.avatar = domain.avatarUrl
        entity.role = domain.role.name
        entity.isActive = domain.isActive
        entity.isEmailVerified = domain.isEmailVerified
        entity.createdAt = domain.createdAt.toOffsetDateTime()
        entity.updatedAt = domain.updatedAt.toOffsetDateTime()
        entity.lastLoginAt = domain.lastLoginAt?.toOffsetDateTime()
        entity.lastLoginIp = domain.lastLoginIp
    }
}