package com.quadro.task.domain.services

import com.lowagie.text.Chunk
import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.task.PeriodReport
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class ReportExportServiceImpl : ReportExportService {
    private val locale = Locale.forLanguageTag("ru")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy").withLocale(locale)

    private fun formatInstant(instant: Instant): String {
        return dateFormatter.format(instant.toJavaInstant().atZone(ZoneId.systemDefault()))
    }

    private fun parseIsoStringToLocalDate(isoString: String): LocalDate? {
        return try {
            val instant = Instant.parse(isoString)
            instant.toJavaInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        } catch (e: Exception) {
            null
        }
    }

    private fun groupByDate(map: Map<String, Long>): Map<LocalDate, Long> {
        return map.entries
            .mapNotNull { entry ->
                parseIsoStringToLocalDate(entry.key)?.let { it to entry.value }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.sum() }
    }


    override fun exportToCsv(report: PeriodReport): String {
        val sb = StringBuilder()
        sb.appendLine("Дата,Создано,Завершено,WIP (среднее)")
        val groupedCreation = groupByDate(report.dailyCreation)
        val groupedCompletion = groupByDate(report.dailyCompletion)
        val allDates = (groupedCreation.keys + groupedCompletion.keys).sorted()

        val russianLocale = Locale.forLanguageTag("ru-RU")

        for (date in allDates) {
            val created = groupedCreation[date] ?: 0
            val completed = groupedCompletion[date] ?: 0
            val dateStr = dateFormatter.format(date)
            val wipStr = String.format(russianLocale, "%.2f", report.wipAverage)
            sb.appendLine("$dateStr,$created,$completed,$wipStr")
        }

        sb.appendLine()
        sb.appendLine("Итого создано,${report.created},,")
        sb.appendLine("Итого завершено,${report.completed},,")
        sb.appendLine("Среднее время выполнения (дней),${String.format(russianLocale, "%.1f", report.averageCompletionDays)},,")
        sb.appendLine("Просрочено на текущую дату,${report.overdueCount},,")
        sb.appendLine("Пропускная способность (задач/день),${String.format(russianLocale, "%.2f", report.throughput)},,")
        sb.appendLine("Эффективность (завершённые/созданные),${String.format(russianLocale, "%.1f%%", report.efficiency)},,")
        return sb.toString()
    }

    override fun exportToPdf(report: PeriodReport): ByteArray {
        val groupedCreation = groupByDate(report.dailyCreation)
        val groupedProgress = groupByDate(report.dailyProgress)
        val groupedCompletion = groupByDate(report.dailyCompletion)
        val allDates = (groupedCreation.keys + groupedCompletion.keys).sorted()

        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A4)
        PdfWriter.getInstance(document, outputStream)
        document.open()

        document.add(Paragraph("Отчёт по проекту", Font(Font.HELVETICA, 18f, Font.BOLD)))
        document.add(Paragraph("Период: ${formatInstant(report.from)} – ${formatInstant(report.to)}", Font(Font.HELVETICA, 12f)))
        document.add(Chunk.NEWLINE)

        val table = PdfPTable(4).apply {
            widthPercentage = 100f
            addCell(createHeaderCell("Дата"))
            addCell(createHeaderCell("Создано"))
            addCell(createHeaderCell("В работе"))
            addCell(createHeaderCell("Завершено"))
        }

        for (date in allDates) {
            val created = groupedCreation[date] ?: 0
            val progress = groupedProgress[date] ?: 0
            val completed = groupedCompletion[date] ?: 0
            val dateStr = dateFormatter.format(date)
            table.addCell(createBodyCell(dateStr))
            table.addCell(createBodyCell(created.toString()))
            table.addCell(createBodyCell(progress.toString()))
            table.addCell(createBodyCell(completed.toString()))
        }
        document.add(table)

        document.add(Chunk.NEWLINE)
        document.add(Paragraph("Общие показатели", Font(Font.HELVETICA, 14f, Font.BOLD)))
        document.add(Paragraph("• Создано задач: ${report.created}"))
        document.add(Paragraph("• Завершено задач: ${report.completed}"))
        document.add(Paragraph("• Среднее время выполнения: ${"%.1f".format(report.averageCompletionDays)} дней"))
        document.add(Paragraph("• Просрочено (на сегодня): ${report.overdueCount}"))
        document.add(Paragraph("• Пропускная способность: ${"%.2f".format(report.throughput)} задач/день"))
        document.add(Paragraph("• Эффективность: ${"%.1f%%".format(report.efficiency)}"))

        document.close()
        return outputStream.toByteArray()
    }

    private fun createHeaderCell(text: String): PdfPCell {
        val cell = PdfPCell(Paragraph(text, Font(Font.HELVETICA, 12f, Font.BOLD)))
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.backgroundColor = Color(220, 220, 220)
        return cell
    }

    private fun createBodyCell(text: String): PdfPCell {
        val cell = PdfPCell(Paragraph(text, Font(Font.HELVETICA, 11f)))
        cell.horizontalAlignment = Element.ALIGN_CENTER
        return cell
    }
}