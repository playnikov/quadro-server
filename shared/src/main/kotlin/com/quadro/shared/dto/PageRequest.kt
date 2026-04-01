package com.quadro.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageRequest(
    val page: Int = 1,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortOrder: String = "desc"
) {
    val offset: Int get() = (page - 1) * size
}
