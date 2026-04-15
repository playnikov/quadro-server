package com.quadro.company.di

import com.quadro.company.domain.repositories.CompanyInvitationRepository
import com.quadro.company.domain.repositories.CompanyMemberRepository
import com.quadro.company.domain.repositories.CompanyRepository
import com.quadro.company.domain.repositories.UserRepository
import com.quadro.company.domain.services.CompanyInvitationService
import com.quadro.company.domain.services.CompanyInvitationServiceImpl
import com.quadro.company.domain.services.CompanyService
import com.quadro.company.domain.services.CompanyServiceImpl
import com.quadro.company.domain.services.InvitationTokenService
import com.quadro.company.domain.services.InvitationTokenServiceImpl
import com.quadro.company.infrastructure.database.repositories.CompanyInvitationRepositoryImpl
import com.quadro.company.infrastructure.database.repositories.CompanyMemberRepositoryImpl
import com.quadro.company.infrastructure.database.repositories.CompanyRepositoryImpl
import com.quadro.company.infrastructure.database.repositories.UserRepositoryImpl
import com.quadro.company.infrastructure.messaging.UserEventProcessor
import com.quadro.company.presentation.controllers.CompanyController
import com.quadro.company.presentation.controllers.InvitationController
import com.quadro.company.presentation.routes.CompanyRoutes
import com.quadro.company.presentation.routes.InvitationRoutes
import com.quadro.shared.security.JwtValidator
import org.koin.dsl.module

fun companyModules() = module {
    single { JwtValidator(get())}

    single<CompanyRepository> { CompanyRepositoryImpl() }
    single<CompanyMemberRepository> { CompanyMemberRepositoryImpl() }
    single<CompanyInvitationRepository> { CompanyInvitationRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl() }

    single<InvitationTokenService> { InvitationTokenServiceImpl(get()) }

    single<CompanyService> { CompanyServiceImpl(get(), get(), get(), get()) }
    single<CompanyInvitationService> { CompanyInvitationServiceImpl(get(), get(), get(), get(), get(), get(), get()) }

    factory { CompanyController(get()) }
    factory { InvitationController(get()) }
    factory { CompanyRoutes(get()) }
    factory { InvitationRoutes(get()) }
}