package com.quadro.di

import com.quadro.datasource.database.DatabaseFactory
import com.quadro.datasource.repositories.*
import com.quadro.domain.services.AuthService
import com.quadro.domain.services.AuthServiceImpl
import com.quadro.domain.services.CompanyService
import com.quadro.domain.services.CompanyServiceImpl
import com.quadro.presentation.auth.AuthController
import com.quadro.presentation.company.CompanyController
import com.quadro.security.BCryptPasswordEncoder
import com.quadro.security.JwtTokenService
import com.quadro.security.JwtTokenServiceImpl
import com.quadro.security.PasswordEncoder
import org.koin.dsl.module

val AppModule = module {
    single { DatabaseFactory.getDataSource() }
    single<UserRepository> { UserRepositoryImpl() }
    single<CompanyRepository> { CompanyRepositoryImpl() }
    single<CompanyMemberRepository> { CompanyMemberRepositoryImpl() }
    single<CompanyInvitationRepository> { CompanyInvitationRepositoryImpl() }
    single<JwtTokenService> { JwtTokenServiceImpl() }
    single<PasswordEncoder> { BCryptPasswordEncoder() }
    single<AuthService> { AuthServiceImpl(get(), get(), get()) }
    single<CompanyService> { CompanyServiceImpl(get(), get(), get(), get()) }

    factory { AuthController(get()) }
    factory { CompanyController(get()) }
}