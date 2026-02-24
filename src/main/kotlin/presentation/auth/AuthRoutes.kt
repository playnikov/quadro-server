package com.quadro.presentation.auth

import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val controller: AuthController by inject()

    route("/api/auth") {
        post("/register") {
            controller.register(call)
        }

        post("/login") {
            controller.login(call)
        }
    }
}