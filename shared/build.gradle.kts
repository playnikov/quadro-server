plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    api(libs.ktor.server.core)
    api(libs.ktor.server.auth)
    api(libs.ktor.server.auth.jwt)
    api(libs.ktor.server.status.pages)
    api(libs.ktor.server.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)
    api(libs.ktor.client.core)
    api(libs.ktor.client.cio)
    api(libs.ktor.client.content.negotiation)

    api(libs.postgresql)
    api(libs.h2)
    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.exposed.java.time)
    api(libs.hikari.cp)
    api(libs.flyway.core)

    api(libs.jbcrypt)
    api(libs.java.jwt)

    api(libs.kafka.clients)

    api(libs.koin.ktor)

    api(libs.koin.logger.slf4j)

    api(libs.ktor.server.metrics)
    api(libs.ktor.server.metrics.micrometer)
    api(libs.micrometer.registry.prometheus)

    api(libs.ktor.server.call.logging)
}

kotlin {
    jvmToolchain(21)
}