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
        }
    }
}