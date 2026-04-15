package com.quadro.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null
) {
    companion object {
        fun <T> ok(data: T) = ApiResponse(success = true, data = data)

        fun error(code: String, message: String) =
            ApiResponse<Nothing>(success = false, error = ApiError(code, message))
    }
}