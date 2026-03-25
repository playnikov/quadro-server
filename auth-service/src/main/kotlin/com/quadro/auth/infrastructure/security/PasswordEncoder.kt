package com.quadro.auth.infrastructure.security

import org.mindrot.jbcrypt.BCrypt

interface PasswordEncoder {
    fun encode(password: String): String
    fun verify(password: String, hash: String): Boolean
}

class BCryptPasswordEncoder : PasswordEncoder {
    override fun encode(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
    override fun verify(password: String, hash: String): Boolean = BCrypt.checkpw(password, hash)
}