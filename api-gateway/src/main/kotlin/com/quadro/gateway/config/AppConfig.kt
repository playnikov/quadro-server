package com.quadro.gateway.config

import com.typesafe.config.ConfigFactory

data class AppConfig(
    val environment: String,
    val server: ServerConfig,
    val redis: RedisConfig,
    val serviceUrls: ServiceUrls,
    val jwt: JwtConfig,
    val rateLimiting: RateLimitingConfig
) {
    companion object {
        fun fromEnvironment(): AppConfig {
            val config = ConfigFactory.load()
            return AppConfig(
                environment = System.getenv("ENVIRONMENT") ?: "development",
                server = ServerConfig(port = (System.getenv("PORT") ?: "8080").toInt()),
                redis = RedisConfig(
                    host = System.getenv("REDIS_HOST") ?: "redis-master",
                    port = (System.getenv("REDIS_PORT") ?: "6379").toInt(),
                    password = System.getenv("REDIS_PASSWORD")
                ),
                serviceUrls = ServiceUrls(
                    auth = System.getenv("AUTH_SERVICE_URL") ?: "http://auth-service:8081",
                    company = System.getenv("COMPANY_SERVICE_URL") ?: "http://company-service:8082",
                    team = System.getenv("TEAM_SERVICE_URL") ?: "http://team-service:8083",
                    project = System.getenv("PROJECT_SERVICE_URL") ?: "http://project-service:8084",
                    task = System.getenv("TASK_SERVICE_URL") ?: "http://task-service:8085",
                    notification = System.getenv("NOTIFICATION_SERVICE_URL") ?: "http://notification-service:8086",
                    activity = System.getenv("ACTIVITY_SERVICE_URL") ?: "http://activity-service:8087"
                ),
                jwt = JwtConfig(
                    secret = System.getenv("JWT_SECRET") ?: "default-secret",
                    issuer = System.getenv("JWT_ISSUER") ?: "quadro",
                    audience = System.getenv("JWT_AUDIENCE") ?: "quadro-api",
                    realm = System.getenv("JWT_REALM") ?: "quadro",
                ),
                rateLimiting = RateLimitingConfig(
                    enabled = config.getBoolean("rateLimiting.enabled"),
                    defaultLimit = config.getInt("rateLimiting.defaultLimit"),
                    defaultWindow = config.getDuration("rateLimiting.defaultWindow").toMillis()
                )
            )
        }
    }
}

data class ServerConfig(val port: Int)
data class RedisConfig(val host: String, val port: Int, val password: String?)
data class ServiceUrls(
    val auth: String, val company: String, val team: String, val project: String,
    val task: String, val notification: String, val activity: String
)
data class JwtConfig(val secret: String, val issuer: String, val audience: String, val realm: String)
data class RateLimitingConfig(val enabled: Boolean, val defaultLimit: Int, val defaultWindow: Long)