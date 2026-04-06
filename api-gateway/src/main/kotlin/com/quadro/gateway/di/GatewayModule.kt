package com.quadro.gateway.di

import com.quadro.gateway.config.AppConfig
import com.quadro.gateway.routes.*
import com.quadro.shared.security.JwtValidator
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
    single { JwtValidator(
        secretKey = get<AppConfig>().jwt.secret,
        issuer = get<AppConfig>().jwt.issuer,
        audience = get<AppConfig>().jwt.audience)
    }

    single<HttpClient> {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 3000
                connectTimeoutMillis = 5000
                socketTimeoutMillis = 3000
            }
            install(DefaultRequest) {
                header("User-Agent", "Quadro-API-Gateway/1.0")
            }
        }
    }

    factory { AuthRoutes(get(), get()) }

    factory { CompanyRoutes(get(), get()) }

    factory { InvitationRoutes(get(), get()) }
}