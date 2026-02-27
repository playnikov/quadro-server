package com.quadro.domain.services

import com.quadro.domain.models.AuthResult
import com.quadro.domain.models.User
import com.quadro.domain.models.UserCreate
import com.quadro.domain.models.UserLogin

interface AuthService {
    suspend fun register(request: UserCreate): Result<AuthResult>
    suspend fun login(request: UserLogin): Result<AuthResult>
    suspend fun validateToken(token: String): Result<User?>
}