package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.UserCreate

interface SeedService {
    suspend fun createSuperAdminIfNotExists(credentials: UserCreate): Boolean
}