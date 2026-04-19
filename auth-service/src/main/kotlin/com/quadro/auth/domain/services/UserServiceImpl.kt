package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.User
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

    override suspend fun getAllUsers(): List<User> {
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