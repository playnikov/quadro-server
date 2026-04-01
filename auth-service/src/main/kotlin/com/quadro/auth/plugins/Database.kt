package com.quadro.auth.plugins

import com.quadro.auth.config.DatabaseConfig
import com.quadro.auth.infrastructure.database.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.apache.kafka.common.serialization.StringSerializer
import org.koin.java.KoinJavaComponent.inject

fun Application.configureDatabase() {
    val config: DatabaseConfig by inject(DatabaseConfig::class.java)
    DatabaseFactory.init(config)
}