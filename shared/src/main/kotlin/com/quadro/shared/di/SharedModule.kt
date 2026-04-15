package com.quadro.shared.di

import com.quadro.shared.data.config.configure
import com.quadro.shared.data.config.loadDatabaseConfig
import com.quadro.shared.data.config.loadJwtConfig
import com.quadro.shared.data.config.loadKafkaConfig
import com.quadro.shared.data.config.loadRedisConfig
import io.ktor.server.application.Application
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun sharedModule(application: Application, serviceId: String) = module {
    single { application.configure() }
    single { application.loadDatabaseConfig() }
    single { application.loadJwtConfig() }
    single { application.loadKafkaConfig(serviceId) }
    single { application.loadRedisConfig() }
    single {
        Json {
            ignoreUnknownKeys  = true
            prettyPrint        = false
            isLenient          = true
            encodeDefaults     = true
            coerceInputValues  = true
        }
    }
}