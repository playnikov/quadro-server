package com.quadro.security

interface PasswordEncoder {
    fun encode(password: String): String
    fun verifyPassword(rawPassword: String, encodedPassword: String): Boolean
}