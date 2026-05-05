package com.quadro.gateway.config

import io.ktor.server.application.Application

data class ServiceUrls(
    val auth: String,
    val team: String,
    val project: String,
    val task: String,
    val notification: String,
)

fun Application.loadServiceUrls() = ServiceUrls(
    auth         = environment.config.property("app.services.auth").getString(),
    team         = environment.config.property("app.services.team").getString(),
    project      = environment.config.property("app.services.project").getString(),
    task         = environment.config.property("app.services.task").getString(),
    notification = environment.config.property("app.services.notification").getString(),
)