package com.quadro.presentation.company.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateCompanyRequest(
    val name: String,
    val description: String? = null,
    val logo: String? = null,
    val website: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val taxId: String? = null
)
