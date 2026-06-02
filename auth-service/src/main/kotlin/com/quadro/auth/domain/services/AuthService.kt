package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserLogin
import java.util.UUID

interface AuthService {
    suspend fun register(request: UserCreate, userAgent: String?): Pair<String, String>
    suspend fun login(request: UserLogin, userAgent: String?): Pair<String, String>
    suspend fun refreshToken(refreshToken: String): Pair<String, String>
    suspend fun validateToken(token: String): User
    suspend fun changePassword(userId: UUID, currentPassword: String, newPassword: String)
    suspend fun changePassword(userId: UUID, newPassword: String)
    suspend fun forgotPassword(email: String)
    suspend fun resetPassword(token: String, newPassword: String)
    suspend fun verifyEmail(token: String)
}