package com.quadro.auth.domain.utils

import com.quadro.shared.dto.DomainException

fun validateEmail(email: String) {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    if (!email.matches(emailRegex.toRegex())) {
        throw DomainException.ValidationError("Invalid email format")
    }
}

fun validateUsername(username: String) {
    if (username.length !in 3..50) {
        throw DomainException.ValidationError("Username must be between 3 and 50 characters")
    }
    if (!username.matches(Regex("^[a-zA-Z0-9._-]+$"))) {
        throw DomainException.ValidationError("Username contains invalid characters")
    }
}

fun validatePassword(password: String) {
    if (password.length < 8) {
        throw DomainException.ValidationError("Password must be at least 8 characters")
    }
    if (!password.any { it.isDigit() }) {
        throw DomainException.ValidationError("Password must contain at least one digit")
    }
    if (!password.any { it.isLetter() }) {
        throw DomainException.ValidationError("Password must contain at least one letter")
    }
}