package com.quadro.auth.presentation.routes

import com.quadro.auth.presentation.controllers.UserController
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class UserRoutes(
    private val controller: UserController
) {
    fun protectedRoute(routing: Route) {
        routing.route("/api/users") {
            get {
                controller.getUsers(call)
            }

            get("/id") {
                controller.getUserById(call)
            }

            get("/ids") {
                controller.getUsersByIds(call)
            }

            get("/profile") {
                controller.getMyProfile(call)
            }

            patch("/update") {
                controller.updateUser(call)
            }

            post("/create") {
                controller.createUser(call)
            }
        }
    }
}