package com.quadro.project.di

import com.quadro.project.domain.repositories.ProjectInvitationRepository
import com.quadro.project.domain.repositories.ProjectMemberRepository
import com.quadro.project.domain.repositories.ProjectRepository
import com.quadro.project.domain.repositories.UserRepository
import com.quadro.project.domain.services.InvitationTokenService
import com.quadro.project.domain.services.InvitationTokenServiceImpl
import com.quadro.project.domain.services.ProjectInvitationService
import com.quadro.project.domain.services.ProjectInvitationServiceImpl
import com.quadro.project.domain.services.ProjectService
import com.quadro.project.domain.services.ProjectServiceImpl
import com.quadro.project.infrastructure.database.repositories.ProjectInvitationRepositoryImpl
import com.quadro.project.infrastructure.database.repositories.ProjectMemberRepositoryImpl
import com.quadro.project.infrastructure.database.repositories.ProjectRepositoryImpl
import com.quadro.project.infrastructure.database.repositories.UserRepositoryImpl
import com.quadro.project.presentation.controllers.InvitationController
import com.quadro.project.presentation.controllers.ProjectController
import com.quadro.project.presentation.routes.InvitationRoutes
import com.quadro.project.presentation.routes.ProjectRoutes
import com.quadro.shared.security.JwtValidator
import org.koin.dsl.module

val projectModules = module {
    single { JwtValidator(get())}
    single<InvitationTokenService> { InvitationTokenServiceImpl(get()) }

    single<UserRepository> { UserRepositoryImpl() }

    single<ProjectMemberRepository> { ProjectMemberRepositoryImpl() }
    single<ProjectInvitationRepository> { ProjectInvitationRepositoryImpl() }
    single<ProjectRepository> { ProjectRepositoryImpl() }

    single<ProjectService> { ProjectServiceImpl(get(), get(), get(), get()) }
    single<ProjectInvitationService> { ProjectInvitationServiceImpl(get(), get(), get(), get(), get(), get(), get()) }

    factory { ProjectController(get()) }
    factory { InvitationController(get()) }

    factory { ProjectRoutes(get()) }
    factory { InvitationRoutes(get()) }
}