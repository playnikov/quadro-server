package com.quadro.gateway.clients

import com.quadro.gateway.config.ServiceUrls
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.slf4j.LoggerFactory

class InvitationServiceClient(
    private val httpClient: HttpClient,
    serviceUrls: ServiceUrls
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val baseUrl = "${serviceUrls.company}/api/companies"

    suspend fun createInvitation(token: String, companyId: String, request: String): HttpResponse {
        logger.debug("Calling companies-service: POST $baseUrl")
        return httpClient.post("$baseUrl/$companyId/invitations/") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun getInvitations(token: String, companyId: String): HttpResponse {
        logger.debug("Calling companies-service: GET $baseUrl")
        return httpClient.get("$baseUrl/$companyId/invitations/") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun cancelInvitation(token: String, companyId: String): HttpResponse {
        logger.debug("Calling companies-service: DELETE $baseUrl")
        return httpClient.delete("$baseUrl/$companyId/invitations/") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun acceptInvitation(token: String, tokenAccept: String): HttpResponse {
        logger.debug("Calling companies-service: POST $baseUrl")
        return httpClient.post("$baseUrl/invite/$tokenAccept") {
            contentType(ContentType.Application.Json)
            parameter("token", tokenAccept)
            header("Authorization", "Bearer $token")
        }
    }
}