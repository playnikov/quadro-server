plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.flyway)
}

application {
    mainClass.set("com.quadro.auth.ApplicationKt")
}

dependencies {
    implementation(project(":shared"))

    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.host.common)
    implementation("io.ktor:ktor-server-cors:3.4.0")
    implementation("io.ktor:ktor-server-rate-limit:3.4.0")

    // Database
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.hikari.cp)
    implementation(libs.flyway.core)

    // Security
    implementation(libs.jbcrypt)
    implementation(libs.java.jwt)

    // Redis
    implementation(libs.lettuce.core)

    // Kafka
    implementation(libs.kafka.clients)

    // DI
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    // Logging
    implementation(libs.logback.classic)

    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}