package com.quadro.gateway.di

import com.quadro.gateway.config.loadServiceUrls
import com.quadro.gateway.routes.*
import com.quadro.shared.data.config.JwtConfig
import com.quadro.shared.security.JwtValidator
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.Application
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun gatewayModule(application: Application) = module {
    single { application.loadServiceUrls() }
    single { JwtValidator(get()) }


    single<HttpClient> {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
                connectTimeoutMillis = 5000
                socketTimeoutMillis = 5000
            }
            install(DefaultRequest) {
                header("User-Agent", "Quadro-API-Gateway/1.0")
            }
            engine {
                maxConnectionsCount = 1000
                endpoint {
                    maxConnectionsPerRoute = 100
                    keepAliveTime          = 5000
                    connectTimeout         = 5000
                }
            }
        }
    }

    single<HttpClient>(named("webSocketClient")) {
        HttpClient(CIO) {
            install(WebSockets)
            install(DefaultRequest) {
                header("User-Agent", "Quadro-API-Gateway/1.0")
            }
            engine {
                maxConnectionsCount = 1000
            }
        }
    }

    factory { AuthRoutes(get(), get()) }

    factory { InvitationRoutes(get(), get()) }

    factory { ProjectRoutes(get(), get()) }

    factory { TeamRoutes(get(), get()) }

    factory { TaskRoutes(get(), get()) }

    factory { WebSocket(get(named("webSocketClient")), get()) }
}