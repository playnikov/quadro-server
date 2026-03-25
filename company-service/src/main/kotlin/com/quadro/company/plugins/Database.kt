package com.quadro.company.plugins

import com.quadro.company.config.DatabaseConfig
import com.quadro.company.infrastructure.database.DatabaseFactory
import io.ktor.server.application.Application
import org.koin.java.KoinJavaComponent.inject

fun Application.configureDatabase() {
    val config: DatabaseConfig by inject(DatabaseConfig::class.java)
    DatabaseFactory.init(config)
}