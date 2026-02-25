package com.quadro.datasource.mappers

import com.quadro.datasource.entities.DbUserRole
import com.quadro.datasource.entities.UserEntity
import com.quadro.domain.models.DomainUserRole
import com.quadro.domain.models.User
import java.time.Instant

object UserMapper {
    fun toDomain(entity: UserEntity): User = User(
        id = entity.id.value,
        email = entity.email,
        username = entity.username,
        avatar = entity.avatar,
        passwordHash = entity.passwordHash,
        firstName = entity.firstName,
        lastName = entity.lastName,
        role = mapToDomainRole(entity.role),
        isActive = entity.isActive,
        isEmailVerified = entity.isEmailVerified,
        createdAt = entity.createdAt.toEpochMilli(),
        updatedAt = entity.updatedAt.toEpochMilli()
    )

    fun toEntity(domain: User): UserEntity = UserEntity.findById(domain.id) ?: UserEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: UserEntity, domain: User) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: UserEntity, domain: User) {
        entity.email = domain.email
        entity.username = domain.username
        entity.avatar = domain.avatar
        entity.passwordHash = domain.passwordHash
        entity.firstName = domain.firstName
        entity.lastName = domain.lastName
        entity.role = mapToDbRole(domain.role)
        entity.isActive = domain.isActive
        entity.isEmailVerified = domain.isEmailVerified
        entity.createdAt = Instant.ofEpochMilli(domain.createdAt)
        entity.updatedAt = Instant.ofEpochMilli(domain.updatedAt)
    }

    private fun mapToDomainRole(dbRole: DbUserRole): DomainUserRole = when (dbRole) {
        DbUserRole.SUPER_ADMIN -> DomainUserRole.SUPER_ADMIN
        DbUserRole.ADMIN -> DomainUserRole.ADMIN
        DbUserRole.PROJECT_MANAGER -> DomainUserRole.PROJECT_MANAGER
        DbUserRole.TEAM_LEAD -> DomainUserRole.TEAM_LEAD
        DbUserRole.USER -> DomainUserRole.USER
        DbUserRole.GUEST -> DomainUserRole.GUEST
    }

    private fun mapToDbRole(domainRole: DomainUserRole): DbUserRole = when (domainRole) {
        DomainUserRole.SUPER_ADMIN -> DbUserRole.SUPER_ADMIN
        DomainUserRole.ADMIN -> DbUserRole.ADMIN
        DomainUserRole.PROJECT_MANAGER -> DbUserRole.PROJECT_MANAGER
        DomainUserRole.TEAM_LEAD -> DbUserRole.TEAM_LEAD
        DomainUserRole.USER -> DbUserRole.USER
        DomainUserRole.GUEST -> DbUserRole.GUEST
    }
}