package com.quadro.company.presentation.routes

import com.quadro.company.presentation.controllers.CompanyController
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class CompanyRoutes(
    private val controller: CompanyController
) {
    fun init(routing: Route) {
        routing.route("/api/companies") {
            authenticate("auth-jwt") {
                post { controller.createCompany(call) }
                get("/my") { controller.getUserCompanies(call) }
                get("/{id}") { controller.getCompany(call) }
                put("/{id}") { controller.updateCompany(call) }
                delete("/{id}") { controller.deleteCompany(call) }
                get("/{id}/members") { controller.getCompanyMembers(call) }
                patch("/{id}/members/{userId}/role") { controller.updateMemberRole(call) }
                delete("/{id}/members/{userId}") { controller.removeMember(call) }
                post("/{id}/leave") { controller.leaveCompany(call) }
            }
        }
    }
}