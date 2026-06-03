package com.quadro.auth.presentation.routes

import com.quadro.auth.presentation.controllers.AuthController
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class AuthRoutes(
    private val authController: AuthController
) {
    fun publicRoute(routing: Route) {
        routing.route("/api/auth") {
            post("/register") {
                authController.register(call)
            }

            post("/login") {
                authController.login(call)
            }

            post("/refresh") {
                authController.refreshToken(call)
            }
        }
    }

    fun protectedRoute(routing: Route) {
        routing.route("/api/auth") {
            patch("/change-password") {
                authController.changePassword(call)
            }
        }
    }
}