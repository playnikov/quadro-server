package com.quadro.di

import com.quadro.datasource.database.DatabaseFactory
import com.quadro.datasource.repositories.company.CompanyInvitationRepository
import com.quadro.datasource.repositories.company.CompanyInvitationRepositoryImpl
import com.quadro.datasource.repositories.company.CompanyMemberRepository
import com.quadro.datasource.repositories.company.CompanyMemberRepositoryImpl
import com.quadro.datasource.repositories.company.CompanyRepository
import com.quadro.datasource.repositories.company.CompanyRepositoryImpl
import com.quadro.datasource.repositories.users.UserRepository
import com.quadro.datasource.repositories.users.UserRepositoryImpl
import com.quadro.domain.services.auth.AuthService
import com.quadro.domain.services.auth.AuthServiceImpl
import com.quadro.domain.services.company.CompanyInvitationService
import com.quadro.domain.services.company.CompanyInvitationServiceImpl
import com.quadro.domain.services.company.CompanyService
import com.quadro.domain.services.company.CompanyServiceImpl
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