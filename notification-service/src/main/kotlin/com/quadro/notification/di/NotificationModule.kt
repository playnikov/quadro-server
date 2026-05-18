package com.quadro.notification.di

import com.quadro.notification.domain.services.NotificationService
import org.koin.dsl.module

val notificationModule = module {
    single { NotificationService() }
}