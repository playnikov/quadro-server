package com.quadro.auth.presentation.controllers

import com.quadro.auth.domain.models.UserResponse
import com.quadro.auth.domain.services.UserService
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import java.util.UUID

class UserController(
    private val userService: UserService
) {
    suspend fun getUsers(call: ApplicationCall) {
        val users = userService.getAllUsers()
        val result = users.map { user ->
            UserResponse.from(user)
        }
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun getUserById(call: ApplicationCall) {
        val userId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("User ID is invalid")
        val user = userService.getUserById(userId)
        call.respond(HttpStatusCode.OK, UserResponse.from(user))
    }

    suspend fun getMyProfile(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val user = userService.getUserById(userId)
        call.respond(HttpStatusCode.OK, UserResponse.from(user))
    }

    suspend fun getUsersByIds(call: ApplicationCall) {
        val idsParam = call.parameters["ids"] ?: throw DomainException.ValidationError("Parameter 'ids' is required")
        val userIds = try {
            idsParam.split(",").map { UUID.fromString(it.trim()) }
        } catch (e: IllegalArgumentException) {
            throw DomainException.ValidationError("One or more User IDs are invalid")
        }

        if (userIds.isEmpty()) {
            call.respond(HttpStatusCode.OK, emptyList<UserResponse>())
            return
        }

        val users = userService.getUsersByIds(userIds)
        val result = users.map { UserResponse.from(it) }
        call.respond(HttpStatusCode.OK, result)
    }
}