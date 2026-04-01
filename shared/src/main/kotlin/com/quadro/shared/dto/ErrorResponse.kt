package com.quadro.shared.dto

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String? = null,
    val details: Map<String, String>? = null,
    val timestamp: Instant = Clock.System.now()
)
