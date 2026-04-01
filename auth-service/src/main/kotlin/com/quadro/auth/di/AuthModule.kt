package com.quadro.auth.di

import com.quadro.auth.config.AppConfig
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.domain.services.AuthService
import com.quadro.auth.domain.services.AuthServiceImpl
import com.quadro.auth.infrastructure.database.repositories.UserRepositoryImpl
import com.quadro.auth.infrastructure.security.BCryptPasswordEncoder
import com.quadro.auth.infrastructure.security.JwtProvider
import com.quadro.auth.infrastructure.security.PasswordEncoder
import com.quadro.auth.presentation.controllers.AuthController
import com.quadro.auth.presentation.routes.AuthRoutes
import com.quadro.shared.security.JwtValidator
import org.koin.dsl.module

fun authModule(appConfig: AppConfig) = module {
    single { appConfig }
    single { appConfig.database }
    single { appConfig.redis }
    single { appConfig.kafka }
    single { appConfig.jwt }
    single { appConfig.minio }

    single<PasswordEncoder> { BCryptPasswordEncoder() }
    single { JwtValidator(
        secretKey = get<AppConfig>().jwt.secret,
        issuer = get<AppConfig>().jwt.issuer,
        audience = get<AppConfig>().jwt.audience)
    }
    single { JwtProvider(get()) }

    single<UserRepository> { UserRepositoryImpl() }

    single<AuthService> { AuthServiceImpl(get(), get(), get(), get(), get()) }

    factory { AuthController(get()) }
    factory { AuthRoutes(get()) }
}