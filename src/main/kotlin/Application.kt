package com.quadro

import com.quadro.datasource.database.DatabaseFactory
import com.quadro.di.AppModule
import com.quadro.plugins.configureApplication
import com.quadro.plugins.configureAuthentication
import com.quadro.plugins.configureRouting
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()

    install(Koin) {
        slf4jLogger()
        modules(AppModule)
    }

    configureApplication()
    configureAuthentication()
    configureRouting()
}
