package com.quadro.shared.data.config

data class DomainConfig(
    val domain: String
)

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int = 10,
    val minIdle: Int = 2,
    val schema: String = "public"
)

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val accessExpiration: Long,
    val refreshExpiration: Long,
    val invitationExpiration: Long
)

data class KafkaConfig(
    val bootstrapServers: String,
    val groupId: String,
    val clientId: String,
)

data class RedisConfig(
    val host: String,
    val port: Int,
    val password: String? = null,
    val database: Int = 0,
)