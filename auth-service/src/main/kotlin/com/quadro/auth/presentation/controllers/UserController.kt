package com.quadro.auth.presentation.controllers

import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserResponse
import com.quadro.auth.domain.services.UserService
import com.quadro.auth.presentation.models.RegisterRequest
import com.quadro.auth.presentation.models.UpdateAdminUserRequest
import com.quadro.shared.dto.ApiResponse
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class UserController(
    private val userService: UserService
) {
    suspend fun getUsers(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden()
        val users = userService.getAllUsers(userId)
        val result = users.map { user ->
            UserResponse.from(user)
        }
        call.respond(HttpStatusCode.OK, ApiResponse.ok(result))
    }

    suspend fun getUserById(call: ApplicationCall) {
        val userId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("User ID is invalid")
        val user = userService.getUserById(userId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(UserResponse.from(user)))
    }

    suspend fun getMyProfile(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val user = userService.getUserById(userId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(UserResponse.from(user)))
    }

    suspend fun getUsersByIds(call: ApplicationCall) {
        val idsParam = call.parameters["ids"] ?: throw DomainException.ValidationError("Parameter 'ids' is required")
        val userIds = try {
            idsParam.split(",").map { UUID.fromString(it.trim()) }
        } catch (e: IllegalArgumentException) {
            throw DomainException.ValidationError("One or more User IDs are invalid")
        }

        if (userIds.isEmpty()) {
            call.respond(HttpStatusCode.OK, ApiResponse.ok(emptyList<UserResponse>()))
            return
        }

        val users = userService.getUsersByIds(userIds)
        val result = users.map { UserResponse.from(it) }
        call.respond(HttpStatusCode.OK, ApiResponse.ok(result))
    }

    suspend fun createUser(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.ValidationError("User ID is invalid")
        val request = call.receive<RegisterRequest>()

        val user = userService.adminCreateUser(userId, UserCreate(
            username = request.username,
            email = request.email,
            lastName = request.lastName,
            firstName = request.firstName,
            middleName = request.middleName,
            isNeedChangePassword = true,
            password = request.password
        ))
        call.respond(HttpStatusCode.OK, ApiResponse.ok(UserResponse.from(user)))
    }

    suspend fun updateUser(call: ApplicationCall) {
        val requesterId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val userId = call.parameters["id"]?.let { UUID.fromString(it) } ?: throw DomainException.ValidationError("User ID is invalid")
        val request = call.receive<UpdateAdminUserRequest>()

        val user = userService.updateUserByAdmin(requesterId, userId, request)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(UserResponse.from(user)))
    }
}