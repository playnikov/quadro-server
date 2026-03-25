package com.quadro.gateway.clients

import com.quadro.gateway.config.ServiceUrls
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class AuthServiceClient(
    private val httpClient: HttpClient,
    private val serviceUrls: ServiceUrls
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun register(request: String): HttpResponse {
        logger.debug("Calling auth-service: POST /api/auth/register")

        return httpClient.post("${serviceUrls.auth}/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun login(request: String): HttpResponse {
        logger.debug("Calling auth-service: POST /api/auth/login")

        return httpClient.post("${serviceUrls.auth}/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun refreshToken(request: String): HttpResponse {
        logger.debug("Calling auth-service: POST /api/auth/refresh")

        return httpClient.post("${serviceUrls.auth}/api/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun validateToken(token: String): HttpResponse {
        logger.debug("Calling auth-service: POST /api/auth/validate")

        return httpClient.post("${serviceUrls.auth}/api/auth/validate") {
            contentType(ContentType.Application.Json)
            setBody("""{"token":"$token"}""")
        }
    }

    suspend fun getUser(userId: String): HttpResponse {
        logger.debug("Calling auth-service: GET /api/users/$userId")

        return httpClient.get("${serviceUrls.auth}/api/users/$userId")
    }

    suspend fun logout(userId: String): HttpResponse {
        logger.debug("Calling auth-service: POST /api/auth/logout")

        return httpClient.post("${serviceUrls.auth}/api/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody("{}")
            header("X-User-Id", userId)
        }
    }

    suspend fun changePassword(userId: String, request: String): HttpResponse {
        logger.debug("Calling auth-service: POST /api/auth/change-password")

        return httpClient.post("${serviceUrls.auth}/api/auth/change-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header("X-User-Id", userId)
        }
    }
}