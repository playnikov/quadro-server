package com.quadro.gateway.routes

import com.quadro.gateway.config.ServiceUrls
import com.quadro.gateway.plugins.proxyTo
import com.quadro.shared.security.getUserId
import io.ktor.client.HttpClient
import io.ktor.server.request.path
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket

class WebSocket(
    private val client: HttpClient,
    serviceBaseUrl: ServiceUrls
) {
    private val baseNotificationUrl = serviceBaseUrl.notification

    fun init(routing: Route) {
        routing.webSocket("/ws/notifications") {
            val userId = call.getUserId() ?: return@webSocket
            val path = call.request.path()
            proxyTo(client, baseNotificationUrl, userId.toString(), path)
        }
    }
}