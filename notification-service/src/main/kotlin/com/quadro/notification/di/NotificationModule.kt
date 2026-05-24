package com.quadro.notification.di

import com.quadro.shared.security.JwtValidator
import org.koin.dsl.module

val notificationModule = module {
    single { JwtValidator(get())}
}