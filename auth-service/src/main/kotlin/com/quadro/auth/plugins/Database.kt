package com.quadro.auth.plugins

import com.quadro.shared.data.config.DatabaseConfig
import com.quadro.shared.data.db.DatabaseFactory
import io.ktor.server.application.Application
import org.koin.ktor.ext.inject

fun Application.configureDatabase() {
    val db by inject<DatabaseConfig>()
    DatabaseFactory.init(db)
}