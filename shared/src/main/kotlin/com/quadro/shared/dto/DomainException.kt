package com.quadro.shared.dto

import java.util.UUID

sealed class DomainException(message: String) : Exception(message) {
    class NotFound(entity: String, id: String) :
        DomainException("$entity with id '$id' not found")

    class AlreadyExists(entity: String) :
        DomainException("$entity already exists")

    class AccessDenied(reason: String = "Access denied") :
        DomainException(reason)
    class Forbidden(reason: String = "Forbidden") :
        DomainException(reason)

    class BusinessRule(rule: String) :
        DomainException(rule)

    class InvalidTransition(from: Any, to: Any) :
        DomainException("Invalid status transition: $from → $to")

    class ValidationError(reason: String) :
        DomainException(reason)
}