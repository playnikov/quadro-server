package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.shared.dto.DomainException
import java.util.UUID

class UserServiceImpl(
    private val userRepository: UserRepository
): UserService {
    override suspend fun getUserById(id: UUID): User {
        val user = userRepository.findById(id)
            ?: throw DomainException.NotFound("User", id.toString())
        return user
    }

    override suspend fun getAllUsers(requesterId: UUID): List<User> {
        val requester = userRepository.findById(requesterId)
            ?: throw DomainException.NotFound("User", requesterId.toString())
        if (!requester.role.isAdmin() && !requester.role.isManager()) {
            throw DomainException.AccessDenied()
        }
        return userRepository.getAll()
    }

    override suspend fun getUserByUsername(username: String): User {
        val user = userRepository.findByUsername(username)
            ?: throw DomainException.NotFound("User", username)
        return user
    }

    override suspend fun getUserByEmail(email: String): User {
        val user = userRepository.findByEmail(email)
            ?: throw DomainException.NotFound("User", email)
        return user
    }

    override suspend fun getUsersByIds(userIds: List<UUID>): List<User> {
        return userRepository.getByIds(userIds)
    }
}