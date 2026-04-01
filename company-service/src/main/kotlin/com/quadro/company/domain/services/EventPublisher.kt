package com.quadro.company.domain.services

import com.quadro.company.domain.models.Company
import kotlin.time.Instant

interface EventPublisher {
    fun publishCompanyCreated(company: Company)
    fun publishCompanyUpdated(company: Company)
    fun publishCompanyDeleted(companyId: String, deletedAt: Instant)
}