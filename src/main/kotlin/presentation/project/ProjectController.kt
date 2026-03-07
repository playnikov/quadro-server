package com.quadro.presentation.project

import com.quadro.domain.services.company.CompanyService
import com.quadro.domain.services.project.ProjectService
import com.quadro.domain.services.team.TeamService
import com.quadro.plugins.getUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory
import java.util.UUID

class ProjectController(
    private val projectService: ProjectService,
    private val companyService: CompanyService,
    private val teamService: TeamService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun createProject(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized)

        try {

        } catch (e: Exception) {
            logger.error("[$requestId] Project creation error", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }
}