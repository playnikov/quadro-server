package com.quadro.team.presentation.routes

import com.quadro.company.presentation.controllers.CompanyController
import com.quadro.company.presentation.controllers.InvitationController
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class CompanyRoutes(
    private val companyController: CompanyController
) {
    fun init(routing: Route) {
        routing.route("/api/companies") {
            authenticate("auth-jwt") {
                post { companyController.createCompany(call) }
                get("/my") { companyController.getUserCompanies(call) }
                get("/{id}") { companyController.getCompany(call) }
                put("/{id}") { companyController.updateCompany(call) }
                delete("/{id}") { companyController.deleteCompany(call) }
                get("/{id}/members") { companyController.getCompanyMembers(call) }
                patch("/{id}/members/{userId}/role") { companyController.updateMemberRole(call) }
                delete("/{id}/members/{userId}") { companyController.removeMember(call) }
                post("/{id}/leave") { companyController.leaveCompany(call) }
            }
        }
    }
}