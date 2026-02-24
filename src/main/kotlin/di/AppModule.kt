package com.quadro.di

import com.quadro.datasource.database.DatabaseFactory
import com.quadro.datasource.repositories.UserRepository
import com.quadro.datasource.repositories.UserRepositoryImpl
import com.quadro.domain.services.AuthService
import com.quadro.domain.services.AuthServiceImpl
import com.quadro.presentation.auth.AuthController
import com.quadro.security.BCryptPasswordEncoder
import com.quadro.security.JwtTokenService
import com.quadro.security.JwtTokenServiceImpl
import com.quadro.security.PasswordEncoder
import org.koin.dsl.module

val AppModule = module {
    single { DatabaseFactory.getDataSource() }
    single<UserRepository> { UserRepositoryImpl() }
    single<JwtTokenService> { JwtTokenServiceImpl() }
    single<PasswordEncoder> { BCryptPasswordEncoder() }
    single<AuthService> { AuthServiceImpl(get(), get(), get()) }

    factory { AuthController(get()) }
}