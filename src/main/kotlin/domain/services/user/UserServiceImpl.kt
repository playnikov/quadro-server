package com.quadro.domain.services.user

import com.quadro.datasource.repositories.users.UserRepository
import com.quadro.domain.models.user.User
import com.quadro.domain.models.user.UserResult
import org.slf4j.LoggerFactory
import java.util.UUID

class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getUser(userId: UUID): Result<UserResult?> {
        return try {
            val user = userRepository.findById(userId)
                ?: return Result.failure(Exception("User not found"))

            Result.success(UserResult.fromUser(user))
        } catch (e: Exception) {
            logger.error("Failed to get user", e)
            Result.failure(e)
        }
    }
}