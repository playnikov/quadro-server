package com.quadro.auth.di

import com.quadro.shared.data.messaging.EventProducer
import org.koin.dsl.module

val kafkaModule = module {
    single { EventProducer(get()) }
}
