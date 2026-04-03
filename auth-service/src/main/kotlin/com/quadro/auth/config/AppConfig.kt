package com.quadro.auth.config

import com.typesafe.config.ConfigFactory


data class AppConfig(
    val environment: String,
    val server: ServerConfig,
    val database: DatabaseConfig,
    val redis: RedisConfig,
    val kafka: KafkaConfig,
    val jwt: JwtConfig,
    val minio: MinioConfig
) {
    companion object {
        fun fromEnvironment(): AppConfig {
            return AppConfig(
                environment = System.getenv("ENVIRONMENT") ?: "development",
                server = ServerConfig(port = (System.getenv("PORT") ?: "8081").toInt()),
                database = DatabaseConfig(
                    host = System.getenv("DB_HOST") ?: "localhost",
                    port = (System.getenv("DB_PORT") ?: "5432").toInt(),
                    name = System.getenv("DB_NAME") ?: "auth",
                    user = System.getenv("DB_USER") ?: "postgres",
                    password = System.getenv("DB_PASSWORD") ?: "postgres",
                    poolSize = (System.getenv("DB_POOL_SIZE") ?: "10").toInt()
                ),
                redis = RedisConfig(
                    host = System.getenv("REDIS_HOST") ?: "localhost",
                    port = (System.getenv("REDIS_PORT") ?: "6379").toInt(),
                    password = System.getenv("REDIS_PASSWORD")
                ),
                kafka = KafkaConfig(
                    brokers = System.getenv("KAFKA_BROKERS") ?: "localhost:9092"
                ),
                jwt = JwtConfig(
                    secret = System.getenv("JWT_SECRET") ?: "default-secret",
                    issuer = System.getenv("JWT_ISSUER") ?: "quadro",
                    audience = System.getenv("JWT_AUDIENCE") ?: "quadro-api",
                    realm = System.getenv("JWT_REALM") ?: "quadro",
                    accessExpiration = (System.getenv("JWT_ACCESS_EXPIRATION") ?: "900000").toLong(),
                    refreshExpiration = (System.getenv("JWT_REFRESH_EXPIRATION") ?: "604800000").toLong()
                ),
                minio = MinioConfig(
                    endpoint = System.getenv("MINIO_ENDPOINT") ?: "http://minio:9000",
                    accessKey = System.getenv("MINIO_ACCESS_KEY") ?: "minioadmin",
                    secretKey = System.getenv("MINIO_SECRET_KEY") ?: "minioadmin",
                    bucket = System.getenv("MINIO_BUCKET") ?: "quadro"
                )
            )
        }
    }
}

data class ServerConfig(val port: Int)
data class DatabaseConfig(
    val host: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String,
    val poolSize: Int,
    val schema: String = "public"
) {
    val jdbcUrl: String get() = "jdbc:postgresql://$host:$port/$name"
}
data class RedisConfig(val host: String, val port: Int, val password: String?)
data class KafkaConfig(val brokers: String)
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val accessExpiration: Long,
    val refreshExpiration: Long
)
data class MinioConfig(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String
)