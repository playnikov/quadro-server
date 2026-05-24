package com.quadro.notification.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.events.UserUpdatedEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class UserEventProcessor(

) : KoinComponent {

    suspend fun processCreated(event: UserCreatedEvent) {

    }

    suspend fun processUpdated(event: UserUpdatedEvent) {

    }
}