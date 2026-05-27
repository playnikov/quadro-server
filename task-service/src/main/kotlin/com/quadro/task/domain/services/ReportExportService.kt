package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.PeriodReport

interface ReportExportService {
    fun exportToCsv(report: PeriodReport): String
    fun exportToPdf(report: PeriodReport): ByteArray
}