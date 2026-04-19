package com.quadro.auth.di

import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.domain.services.AuthService
import com.quadro.auth.domain.services.AuthServiceImpl
import com.quadro.auth.infrastructure.database.repositories.UserRepositoryImpl
import com.quadro.auth.infrastructure.security.BCryptPasswordEncoder
import com.quadro.auth.infrastructure.security.JwtProvider
import com.quadro.auth.infrastructure.security.PasswordEncoder
import com.quadro.auth.presentation.controllers.AuthController
import com.quadro.auth.presentation.routes.AuthRoutes
import com.quadro.shared.data.db.DatabaseFactory
import com.quadro.shared.security.JwtValidator
import io.ktor.server.application.Application
import org.koin.dsl.module

val authModule = module {
    single { JwtValidator(get()) }
    single<PasswordEncoder> { BCryptPasswordEncoder() }
    single { JwtProvider(get()) }

    single<AuthService> { AuthServiceImpl(get(), get(), get(), get(), get()) }

    factory { AuthController(get()) }
    factory { AuthRoutes(get()) }
}