package com.quadro.auth.di

import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.domain.services.UserService
import com.quadro.auth.domain.services.UserServiceImpl
import com.quadro.auth.infrastructure.database.repositories.UserRepositoryImpl
import com.quadro.auth.presentation.controllers.UserController
import com.quadro.auth.presentation.routes.UserRoutes
import org.koin.dsl.module

val userModule = module {
    single<UserRepository> { UserRepositoryImpl() }

    single<UserService> { UserServiceImpl(get()) }
    factory { UserController(get()) }
    factory { UserRoutes(get()) }
}