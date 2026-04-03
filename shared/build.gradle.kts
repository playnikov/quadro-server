plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.server.auth.jwt)
    api(libs.java.jwt)
    api(libs.slf4j.api)
    api(libs.ktor.server.call.logging)
    api(libs.ktor.server.status.pages)
    api(libs.ktor.server.metrics)
    api(libs.ktor.server.metrics.micrometer)
    api(libs.micrometer.registry.prometheus)
    api(libs.kafka.clients)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockk)
}

kotlin {
    jvmToolchain(21)
}