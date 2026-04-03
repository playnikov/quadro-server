package com.quadro.domain.services.user

import com.quadro.domain.models.user.UserResult
import java.util.UUID


interface UserService {
    suspend fun getUser(userId: UUID): Result<UserResult?>
}