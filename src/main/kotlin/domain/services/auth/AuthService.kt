package com.quadro.domain.services.auth

import com.quadro.domain.models.user.AuthResult
import com.quadro.domain.models.user.User
import com.quadro.domain.models.user.UserCreate
import com.quadro.domain.models.user.UserLogin

interface AuthService {
    suspend fun register(request: UserCreate): Result<AuthResult>
    suspend fun login(request: UserLogin): Result<AuthResult>
    suspend fun validateToken(token: String): Result<User?>
}