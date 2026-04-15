package com.quadro.shared.data.messaging.events

sealed interface DomainEvent {
    val eventId: String
    val occurredAt: Long
    val version: Int
}