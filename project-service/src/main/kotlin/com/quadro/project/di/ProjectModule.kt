package com.quadro.project.di

import com.quadro.project.domain.repositories.CompanyMemberRepository
import com.quadro.project.domain.repositories.CompanyRepository
import com.quadro.project.domain.repositories.ProjectRepository
import com.quadro.project.domain.repositories.UserRepository
import com.quadro.project.domain.services.ProjectService
import com.quadro.project.domain.services.ProjectServiceImpl
import com.quadro.project.infrastructure.database.repositories.CompanyMemberRepositoryImpl
import com.quadro.project.infrastructure.database.repositories.CompanyRepositoryImpl
import com.quadro.project.infrastructure.database.repositories.ProjectRepositoryImpl
import com.quadro.project.infrastructure.database.repositories.UserRepositoryImpl
import com.quadro.project.presentation.controllers.ProjectController
import com.quadro.project.presentation.routes.ProjectRoutes
import com.quadro.shared.security.JwtValidator
import org.koin.dsl.module

val projectModules = module {
    single { JwtValidator(get())}

    single<CompanyRepository> { CompanyRepositoryImpl() }
    single<CompanyMemberRepository> { CompanyMemberRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl() }

    single<ProjectRepository> { ProjectRepositoryImpl() }

    single<ProjectService> { ProjectServiceImpl(get(), get(), get(), get(), get()) }

    factory { ProjectController(get()) }
    factory { ProjectRoutes(get()) }
}