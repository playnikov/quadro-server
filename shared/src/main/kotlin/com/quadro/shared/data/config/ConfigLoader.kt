package com.quadro.shared.data.config

import io.ktor.server.application.Application

fun Application.configure() = DomainConfig(
    domain = environment.config.propertyOrNull("app.domain")?.getString() ?: "localhost"
)
fun Application.loadDatabaseConfig() = DatabaseConfig(
    url      = environment.config.property("app.database.url").getString(),
    user     = environment.config.property("app.database.user").getString(),
    password = environment.config.property("app.database.password").getString(),
    maxPoolSize = environment.config.propertyOrNull("app.database.max_pool_size")?.getString()?.toInt() ?: 10,
    minIdle     = environment.config.propertyOrNull("app.database.min_idle")?.getString()?.toInt() ?: 2,
    schema = environment.config.property("app.database.schema").getString(),
)

fun Application.loadJwtConfig() = JwtConfig(
    secret      = environment.config.property("app.jwt.secret").getString(),
    issuer      = environment.config.property("app.jwt.issuer").getString(),
    audience    = environment.config.property("app.jwt.audience").getString(),
    accessExpiration = environment.config.propertyOrNull("app.jwt.access_ms")?.getString()?.toLong() ?: 3600000,
    refreshExpiration = environment.config.propertyOrNull("app.jwt.refresh_ms")?.getString()?.toLong() ?: 604800000,
    invitationExpiration = environment.config.propertyOrNull("app.jwt.invitation_ms")?.getString()?.toLong() ?: 604800000,
)

fun Application.loadKafkaConfig(serviceId: String) = KafkaConfig(
    bootstrapServers = environment.config.property("app.kafka.bootstrap_servers").getString(),
    groupId          = environment.config.propertyOrNull("app.kafka.group_id")?.getString() ?: "quadro-$serviceId",
    clientId         = environment.config.propertyOrNull("app.kafka.client_id")?.getString() ?: "quadro-$serviceId",
)

fun Application.loadRedisConfig() = RedisConfig(
    host     = environment.config.propertyOrNull("app.redis.host")?.getString() ?: "localhost",
    port     = environment.config.propertyOrNull("app.redis.port")?.getString()?.toInt() ?: 6379,
    password = environment.config.propertyOrNull("app.redis.password")?.getString(),
)