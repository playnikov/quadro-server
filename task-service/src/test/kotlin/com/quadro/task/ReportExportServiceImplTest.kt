package com.quadro.task

import com.quadro.task.domain.models.task.PeriodReport
import com.quadro.task.domain.services.ReportExportServiceImpl
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class ReportExportServiceImplTest {
    private val service = ReportExportServiceImpl()

    @Test
    fun `exportToCsv should produce valid CSV content`() = runTest {
        val now = Instant.parse("2025-01-01T00:00:00Z")
        val from = now - 2.days
        val report = PeriodReport(
            from = from,
            to = now,
            created = 5L,
            completed = 3L,
            statusBreakdown = emptyMap(),
            dailyCreation = mapOf("2024-12-30T15:34:58.575Z" to 2L, "2024-12-31T15:34:58.575Z" to 3L),
            dailyProgress = mapOf("2024-12-30T15:34:58.575Z" to 1L, "2024-12-31T15:34:58.575Z" to 2L),
            dailyCompletion = mapOf("2024-12-31T15:34:58.575Z" to 3L),
            averageCompletionDays = 2.5,
            overdueCount = 1L,
            throughput = 1.5,
            efficiency = 60.0,
            wipAverage = 2.0
        )
        val csv = service.exportToCsv(report)
        assertTrue(csv.contains("Дата,Создано,Завершено,WIP (среднее)"))
        assertTrue(csv.contains("30 декабря 2024,2,0,2,00"))
        assertTrue(csv.contains("31 декабря 2024,3,3,2,00"))
        assertTrue(csv.contains("Итого создано,5,,"))
        assertTrue(csv.contains("Итого завершено,3,,"))
        assertTrue(csv.contains("Среднее время выполнения (дней),2,5,,"))
        assertTrue(csv.contains("Просрочено на текущую дату,1,,"))
        assertTrue(csv.contains("Пропускная способность (задач/день),1,50,,"))
        assertTrue(csv.contains("Эффективность (завершённые/созданные),60,0%,,"))
    }

    @Test
    fun `exportToPdf should return non-empty byte array`() = runTest {
        val now = Instant.parse("2025-01-01T00:00:00Z")
        val from = now - 2.days
        val report = PeriodReport(
            from = from,
            to = now,
            created = 5L,
            completed = 3L,
            statusBreakdown = emptyMap(),
            dailyCreation = mapOf("2024-12-30T15:34:58.575Z" to 2L, "2024-12-31T15:34:58.575Z" to 3L),
            dailyProgress = mapOf("2024-12-30T15:34:58.575Z" to 1L, "2024-12-31T15:34:58.575Z" to 2L),
            dailyCompletion = mapOf("2024-12-31T15:34:58.575Z" to 3L),
            averageCompletionDays = 2.5,
            overdueCount = 1L,
            throughput = 1.5,
            efficiency = 60.0,
            wipAverage = 2.0
        )

        val pdfBytes = service.exportToPdf(report)

        assertNotNull(pdfBytes)
        assertTrue(pdfBytes.isNotEmpty())
        // Check PDF magic number
        assertEquals('%'.code.toByte(), pdfBytes[0])
        assertEquals('P'.code.toByte(), pdfBytes[1])
        assertEquals('D'.code.toByte(), pdfBytes[2])
        assertEquals('F'.code.toByte(), pdfBytes[3])
    }
}