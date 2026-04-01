package com.quadro.shared.plugins

import com.codahale.metrics.Slf4jReporter
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.metrics.dropwizard.DropwizardMetrics
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.event.Level
import java.util.concurrent.TimeUnit

fun Application.configureMonitoring() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> !call.request.path().startsWith("/health") }
        format { call ->
            val status = call.response.status()?.value ?: 0
            val method = call.request.httpMethod.value
            val path = call.request.path()
            val elapsed = call.processingTimeMillis()
            "$status $method $path (${elapsed}ms)"
        }
    }

    routing {
        get("/metrics") {
            call.respondText(appMicrometerRegistry.scrape(), ContentType.Text.Plain)
        }
    }
}