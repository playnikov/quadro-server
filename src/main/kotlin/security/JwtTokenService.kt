package com.quadro.security

import com.quadro.domain.models.user.TokenValidationResult
import com.quadro.domain.models.user.User

interface JwtTokenService {
    fun generateAccessToken(user: User): String
    fun generateRefreshToken(user: User): String
    fun validateToken(token: String): TokenValidationResult
}