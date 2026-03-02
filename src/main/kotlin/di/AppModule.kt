package com.quadro.di

import com.quadro.datasource.database.DatabaseFactory
import com.quadro.datasource.repositories.*
import com.quadro.domain.services.*
import com.quadro.presentation.auth.AuthController
import com.quadro.presentation.company.CompanyController
import com.quadro.security.*
import org.koin.dsl.module

val AppModule = module {
    single { DatabaseFactory.getDataSource() }
    single<UserRepository> { UserRepositoryImpl() }
    single<CompanyRepository> { CompanyRepositoryImpl() }
    single<CompanyMemberRepository> { CompanyMemberRepositoryImpl() }
    single<CompanyInvitationRepository> { CompanyInvitationRepositoryImpl() }
    single<JwtTokenService> { JwtTokenServiceImpl() }
    single<JwtInvitationTokenService> { JwtInvitationTokenServiceImpl() }
    single<PasswordEncoder> { BCryptPasswordEncoder() }
    single<AuthService> { AuthServiceImpl(get(), get(), get()) }
    single<CompanyService> { CompanyServiceImpl(get(), get(), get(), get()) }
    single<CompanyInvitationService> { CompanyInvitationServiceImpl(get(), get(), get(), get(), get()) }


    factory { AuthController(get()) }
    factory { CompanyController(get(), get()) }
}