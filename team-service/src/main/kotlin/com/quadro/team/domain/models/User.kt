package com.quadro.team.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

data class User(
    val id: UUID,
    val isActive: Boolean,
)
