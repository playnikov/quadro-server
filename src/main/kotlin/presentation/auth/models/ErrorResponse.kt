package com.quadro.presentation.auth.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ErrorResponse(
    val message: String,
    val timestamp: String = LocalDateTime.now().toString()
)
