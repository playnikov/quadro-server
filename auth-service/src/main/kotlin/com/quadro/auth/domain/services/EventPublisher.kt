package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.User
import kotlin.time.Instant

interface EventPublisher {
    fun publishUserCreated(user: User)
    fun publishUserUpdated(user: User)
    fun publishUserDeleted(userId: String, deletedAt: Instant)
}