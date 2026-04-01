package com.quadro.company.domain.services

import com.quadro.company.domain.models.Company
import com.quadro.shared.events.CompanyEvent
import com.quadro.shared.events.UserEvent
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import kotlin.time.Instant

class EventPublisherImpl(
    private val producer: KafkaProducer<String, String>
) : EventPublisher {
    private val json = Json { ignoreUnknownKeys = true }

    override fun publishCompanyCreated(company: Company) {
        val event = CompanyEvent.Created(
            companyId = company.id.toString(),
            name = company.name,
            status = company.companyStatus.name,
            currentProjects = company.currentProjects,
            maxProjects = company.maxProjects,
            updatedAt = company.updatedAt
        )
        sendEvent(event.companyId, event)
    }

    override fun publishCompanyUpdated(company: Company) {
        val event = CompanyEvent.Updated(
            companyId = company.id.toString(),
            name = company.name,
            status = company.companyStatus.name,
            currentProjects = company.currentProjects,
            maxProjects = company.maxProjects,
            updatedAt = company.updatedAt
        )
        sendEvent(event.companyId, event)
    }

    override fun publishCompanyDeleted(companyId: String, deletedAt: Instant) {
        val event = CompanyEvent.Deleted(
            companyId = companyId,
            deletedAt = deletedAt
        )
        sendEvent(event.companyId, event)
    }

    private fun sendEvent(companyId: String, event: CompanyEvent) {
        val record = ProducerRecord("company-events", companyId, json.encodeToString(event))
        producer.send(record)
    }
}