package com.quadro.presentation.company

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.companyRoutes() {
    val controller: CompanyController by inject()

    route("/invite") {
        authenticate("auth-jwt") {
            post {
                controller.acceptInvitation(call)
            }
        }
    }

    route("/api/companies") {
        authenticate("auth-jwt") {
            post {
                controller.createCompany(call)
            }

            get("/my") {
                controller.getUserCompanies(call)
            }

            get("/{id}") {
                controller.getCompany(call)
            }

            put("/{id}") {
                controller.updateCompany(call)
            }

            delete("/{id}") {
                controller.deleteCompany(call)
            }

            post("/{id}/invitations") {
                controller.createInvitation(call)
            }

            get("/{id}/invitations") {
                controller.getInvitations(call)
            }

            delete("/{id}/invitations/{invitationId}") {
                controller.cancelInvitation(call)
            }

            post("/{id}/invitations/{invitationId}/resend") {
                controller.resendInvitation(call)
            }
        }
    }
}