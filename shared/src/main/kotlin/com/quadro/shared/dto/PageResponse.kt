package com.quadro.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class PagedResponse<T>(
    val items: List<T>,
    val total: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int,
) {
    companion object {
        fun <T> of(items: List<T>, page: Int, size: Int, total: Long) = PagedResponse(
            items = items, page = page, size = size, total = total,
            totalPages = if (size == 0) 0 else ((total + size - 1) / size).toInt(),
        )
    }
}