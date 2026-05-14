package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserRole
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.presentation.models.UpdateUserRequest
import com.quadro.shared.dto.DomainException
import java.util.UUID
import kotlin.time.Clock

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

    override suspend fun updateUser(requesterId: UUID, userId: UUID, request: UpdateUserRequest): User {
        val requester = userRepository.findById(requesterId)
            ?: throw DomainException.NotFound("User", requesterId.toString())

        if (!requester.role.isAdmin()) {
            throw DomainException.AccessDenied()
        }

        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", userId.toString())

        val updateUser = user.copy(
            username = request.username ?: user.username,
            email = request.email ?: user.email,
            firstName = request.firstName ?: user.firstName,
            lastName = request.lastName ?: user.lastName,
            middleName = request.middleName ?: user.middleName,
            role = request.role ?: user.role,
            isActive = request.isActive ?: user.isActive,
            updatedAt = Clock.System.now()
        )

        return userRepository.upsert(updateUser)
    }
}