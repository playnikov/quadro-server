package com.quadro.notification.plugins

import com.quadro.shared.data.config.DatabaseConfig
import com.quadro.shared.data.db.DatabaseFactory
import io.ktor.server.application.Application
import org.koin.java.KoinJavaComponent.inject
import kotlin.getValue

fun Application.configureDatabase() {
    val config: DatabaseConfig by inject(DatabaseConfig::class.java)
    DatabaseFactory.init(config)
}