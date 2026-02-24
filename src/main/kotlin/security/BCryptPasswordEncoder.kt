package com.quadro.security

import java.security.MessageDigest
import java.util.Base64

class BCryptPasswordEncoder : PasswordEncoder {
    override fun encode(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }

    override fun verifyPassword(rawPassword: String, encodedPassword: String): Boolean {
        return encode(rawPassword) == encodedPassword
    }
}