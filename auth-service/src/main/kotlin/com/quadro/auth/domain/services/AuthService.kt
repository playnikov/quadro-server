package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.AuthResult
import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserLogin
import com.quadro.auth.domain.models.UserResult
import java.util.UUID

interface AuthService {
    suspend fun register(request: UserCreate, ipAddress: String?): Result<AuthResult>
    suspend fun login(request: UserLogin, ipAddress: String?, userAgent: String?): Result<AuthResult>
    suspend fun refreshToken(refreshToken: String): Result<AuthResult>
    suspend fun validateToken(token: String): Result<UserResult>
    suspend fun changePassword(userId: UUID, currentPassword: String, newPassword: String): Result<Unit>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>
    suspend fun verifyEmail(token: String): Result<Unit>
    suspend fun logout(userId: UUID): Result<Unit>
    suspend fun getUser(userId: UUID): Result<UserResult>
}