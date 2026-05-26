package com.quadro.gateway.routes

import com.quadro.gateway.config.ServiceUrls
import com.quadro.gateway.plugins.proxyTo
import com.quadro.shared.security.JwtValidator
import com.quadro.shared.security.getUserId
import io.ktor.client.HttpClient
import io.ktor.server.request.path
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.ktor.ext.getKoin
import org.koin.mp.KoinPlatform.getKoin

class WebSocket(
    private val client: HttpClient,
    serviceBaseUrl: ServiceUrls
) {
    private val baseNotificationUrl = serviceBaseUrl.notification
    private val jwtValidator: JwtValidator = getKoin().get<JwtValidator>()

    fun init(routing: Route) {
        routing.webSocket("/ws/notifications") {
            val authFrame = incoming.receive() as? Frame.Text
                ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Expected auth message"))

            val authText = authFrame.readText()
            val json = Json.parseToJsonElement(authText).jsonObject
            val token = json["token"]?.jsonPrimitive?.content
            if (token == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing token"))
                return@webSocket
            }

            val userId = jwtValidator.validateToken(token).userId
            if (userId == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
                return@webSocket
            }

            send(Frame.Text("""{"type":"auth_success"}"""))

            val path = call.request.path()
            val projectIds = call.request.queryParameters["projectIds"] ?: ""
            proxyTo(
                client = client,
                baseUrl = baseNotificationUrl,
                userId = userId.toString(),
                path = path,
                queryParams = mapOf(
                    "projectIds" to projectIds
                )
            )
        }
    }
}