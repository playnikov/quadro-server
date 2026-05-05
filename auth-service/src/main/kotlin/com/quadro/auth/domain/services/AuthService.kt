package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.AuthResult
import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserLogin
import com.quadro.auth.domain.models.UserResponse
import java.util.UUID

interface AuthService {
    suspend fun register(request: UserCreate, ipAddress: String?): AuthResult
    suspend fun login(request: UserLogin, ipAddress: String?, userAgent: String?): AuthResult
    suspend fun refreshToken(refreshToken: String): AuthResult
    suspend fun validateToken(token: String): UserResponse
    suspend fun changePassword(userId: UUID, currentPassword: String, newPassword: String)
    suspend fun forgotPassword(email: String)
    suspend fun resetPassword(token: String, newPassword: String)
    suspend fun verifyEmail(token: String)
}