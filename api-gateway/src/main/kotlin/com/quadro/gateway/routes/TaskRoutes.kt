package com.quadro.gateway.routes

import com.quadro.gateway.config.ServiceUrls
import com.quadro.gateway.plugins.proxyTo
import io.ktor.client.HttpClient
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

class TaskRoutes(
    private val client: HttpClient,
    serviceBaseUrl: ServiceUrls
) {
    private val taskServiceBaseUrl = serviceBaseUrl.task

    fun protectedRoutes(routing: Route) {
        routing.route("/api/tasks") {
            proxyTo(client, taskServiceBaseUrl)

            route("/project") {
                proxyTo(client, taskServiceBaseUrl)
            }
            route("/sprint") {
                proxyTo(client, taskServiceBaseUrl)
            }
            route("/assignee") {
                proxyTo(client, taskServiceBaseUrl)
            }
            route("/team/project") {
                proxyTo(client, taskServiceBaseUrl)
            }
            route("/parent") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/validate/status/{status}") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/status/{status}") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/start") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/complete") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/cancel") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/reopen") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/assign/user") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/assign/team") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/unassign") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/validate/user") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/validate/team") {
                proxyTo(client, taskServiceBaseUrl)
            }
        }

        routing.route("/api/tasks/reporting") {
            route("/backlog") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/todo") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/in-progress") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/in-review") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/done") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/cancelled") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/counts") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/overdue") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/avg-completion") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/completion-rate") {
                proxyTo(client, taskServiceBaseUrl)
            }

            route("/velocity") {
                proxyTo(client, taskServiceBaseUrl)
            }
        }

        routing.route("/api/sprints") {
            proxyTo(client, taskServiceBaseUrl)

            route("/project") {
                proxyTo(client, taskServiceBaseUrl)
            }
        }
    }
}