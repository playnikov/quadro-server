package com.quadro.gateway.plugins

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import java.util.UUID

fun ApplicationCall.principalUserId(): String? {
    return principal<UserIdPrincipal>()?.name
}