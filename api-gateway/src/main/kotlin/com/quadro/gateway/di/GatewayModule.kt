package com.quadro.gateway.di

import com.quadro.gateway.clients.AuthServiceClient
import com.quadro.gateway.clients.CompanyServiceClient
import com.quadro.gateway.clients.InvitationServiceClient
import com.quadro.gateway.config.AppConfig
import com.quadro.gateway.routes.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun gatewayModule(appConfig: AppConfig) = module {
    single { appConfig }
    single { appConfig.serviceUrls }
    single { appConfig.jwt }
    single { appConfig.rateLimiting }

    single<HttpClient> {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 5000
                socketTimeoutMillis = 30000
            }
            install(DefaultRequest) {
                header("User-Agent", "Quadro-API-Gateway/1.0")
            }
        }
    }

    single { AuthServiceClient(get(), get()) }
    factory { AuthRoutes(get()) }

    single { CompanyServiceClient(get(), get()) }
    factory { CompanyRoutes(get()) }

    single { InvitationServiceClient(get(), get()) }
    factory { InvitationRoutes(get()) }
}