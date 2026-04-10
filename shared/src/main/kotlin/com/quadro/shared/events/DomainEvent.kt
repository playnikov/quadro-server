package com.quadro.shared.events

sealed interface DomainEvent {
    val eventId: String
    val occurredAt: Long
    val version: Int
}