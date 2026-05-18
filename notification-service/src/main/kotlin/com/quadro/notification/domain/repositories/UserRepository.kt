package com.quadro.notification.domain.repositories

import com.quadro.notification.domain.models.User

interface UserRepository {
    suspend fun upsert(user: User)
}