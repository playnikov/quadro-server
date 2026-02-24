package com.quadro.security

import com.quadro.domain.models.TokenValidationResult
import com.quadro.domain.models.User

interface JwtTokenService {
    fun generateAccessToken(user: User): String
    fun generateRefreshToken(user: User): String
    fun validateToken(token: String): TokenValidationResult
}