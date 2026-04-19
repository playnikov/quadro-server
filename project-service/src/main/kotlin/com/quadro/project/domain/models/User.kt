package com.quadro.project.domain.models

import java.util.UUID

data class User(
    val id: UUID,
    val isActive: Boolean,
)
