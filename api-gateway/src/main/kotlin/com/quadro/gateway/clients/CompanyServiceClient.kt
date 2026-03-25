package com.quadro.gateway.clients

import com.quadro.gateway.config.ServiceUrls
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class CompanyServiceClient(
    private val httpClient: HttpClient,
    serviceUrls: ServiceUrls
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val baseUrl = "${serviceUrls.company}/api/companies"

    suspend fun createCompany(token: String, request: String): HttpResponse {
        logger.debug("Calling companies-service: POST $baseUrl")
        return httpClient.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun getUserCompanies(token: String, filter: String? = null): HttpResponse {
        logger.debug("Calling companies-service: GET $baseUrl")
        return httpClient.get("$baseUrl/my") {
            header("Authorization", "Bearer $token")
            filter?.let { parameter("filter", it) }
        }
    }

    suspend fun getCompany(token: String, companyId: String): HttpResponse {
        logger.debug("Calling companies-service: GET $baseUrl")
        return httpClient.get("$baseUrl/$companyId") {
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun updateCompany(token: String, companyId: String, request: String): HttpResponse {
        logger.debug("Calling companies-service: PUT $baseUrl")
        return httpClient.put("$baseUrl/$companyId") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun deleteCompany(token: String, companyId: String): HttpResponse {
        logger.debug("Calling companies-service: DELETE $baseUrl")
        return httpClient.delete("$baseUrl/$companyId") {
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun getCompanyMembers(token: String, companyId: String): HttpResponse {
        logger.debug("Calling companies-service: GET $baseUrl")
        return httpClient.get("$baseUrl/$companyId/members") {
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun updateMemberRole(token: String, companyId: String, userId: String, request: String): HttpResponse {
        logger.debug("Calling companies-service: PUT $baseUrl")
        return httpClient.put("$baseUrl/$companyId/members/$userId/role") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun removeMember(token: String, companyId: String, userId: String): HttpResponse {
        logger.debug("Calling companies-service: DELETE $baseUrl")
        return httpClient.delete("$baseUrl/$companyId/members/$userId") {
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun leaveCompany(token: String, companyId: String): HttpResponse {
        logger.debug("Calling companies-service: POST $baseUrl")
        return httpClient.post("$baseUrl/$companyId/leave") {
            header("Authorization", "Bearer $token")
        }
    }
}