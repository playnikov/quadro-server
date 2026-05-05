package com.quadro.team.di

import com.quadro.shared.security.JwtValidator
import com.quadro.team.domain.repositories.ProjectRepository
import com.quadro.team.domain.repositories.TeamMemberRepository
import com.quadro.team.domain.repositories.TeamProjectBindingRepository
import com.quadro.team.domain.repositories.TeamRepository
import com.quadro.team.domain.repositories.UserRepository
import com.quadro.team.domain.services.ProjectBindingService
import com.quadro.team.domain.services.ProjectBindingServiceImpl
import com.quadro.team.domain.services.TeamMemberService
import com.quadro.team.domain.services.TeamMemberServiceImpl
import com.quadro.team.domain.services.TeamService
import com.quadro.team.domain.services.TeamServiceImpl
import com.quadro.team.infrastructure.database.repositories.ProjectRepositoryImpl
import com.quadro.team.infrastructure.database.repositories.TeamMemberRepositoryImpl
import com.quadro.team.infrastructure.database.repositories.TeamProjectBindingRepositoryImpl
import com.quadro.team.infrastructure.database.repositories.TeamRepositoryImpl
import com.quadro.team.infrastructure.database.repositories.UserRepositoryImpl
import com.quadro.team.presentation.controllers.ProjectBindingController
import com.quadro.team.presentation.controllers.TeamController
import com.quadro.team.presentation.controllers.TeamMemberController
import com.quadro.team.presentation.routes.BindingRoutes
import com.quadro.team.presentation.routes.TeamMemberRoutes
import com.quadro.team.presentation.routes.TeamRoutes
import org.koin.dsl.module

val teamModules = module {
    single { JwtValidator(get()) }

    single<TeamRepository> { TeamRepositoryImpl() }
    single<TeamMemberRepository> { TeamMemberRepositoryImpl() }
    single<TeamProjectBindingRepository> { TeamProjectBindingRepositoryImpl() }

    single<ProjectRepository> { ProjectRepositoryImpl() }

    single<UserRepository> { UserRepositoryImpl() }

    single<TeamService> { TeamServiceImpl(get(), get(), get(), get(), get()) }
    single<TeamMemberService> { TeamMemberServiceImpl(get(), get(), get()) }
    single<ProjectBindingService> { ProjectBindingServiceImpl(get(), get(), get(), get()) }

    factory { TeamController(get()) }
    factory { TeamMemberController(get()) }
    factory { ProjectBindingController(get()) }

    factory { TeamRoutes(get()) }
    factory { TeamMemberRoutes(get()) }
    factory { BindingRoutes(get()) }
}