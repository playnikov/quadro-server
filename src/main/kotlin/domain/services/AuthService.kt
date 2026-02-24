package com.quadro.domain.services

import com.quadro.domain.models.AuthResponse
import com.quadro.domain.models.User
import com.quadro.domain.models.UserCreate
import com.quadro.domain.models.UserLogin

interface AuthService {
    suspend fun register(request: UserCreate): Result<AuthResponse>
    suspend fun login(request: UserLogin): Result<AuthResponse>
    suspend fun validateToken(token: String): Result<User?>
}