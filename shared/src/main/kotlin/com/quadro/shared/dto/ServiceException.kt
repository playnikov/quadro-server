package com.quadro.shared.dto

class ServiceException(
    message: String,
    val statusCode: Int = 500
) : Exception(message)