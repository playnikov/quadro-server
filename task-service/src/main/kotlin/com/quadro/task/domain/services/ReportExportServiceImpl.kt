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
import com.quadro.task.domain.models.task.PeriodReport
import java.awt.Color
import java.io.ByteArrayOutputStream

class ReportExportServiceImpl : ReportExportService {
    override fun exportToCsv(report: PeriodReport): String {
        val sb = StringBuilder()
        sb.appendLine("Дата,Создано,Завершено,WIP (среднее)")
        val allDates = (report.dailyCreation.keys + report.dailyCompletion.keys).sorted()
        for (date in allDates) {
            val created = report.dailyCreation[date] ?: 0
            val completed = report.dailyCompletion[date] ?: 0
            sb.appendLine("${date},${created},${completed},${"%.2f".format(report.wipAverage)}")
        }

        sb.appendLine()
        sb.appendLine("Итого создано,${report.created},,")
        sb.appendLine("Итого завершено,${report.completed},,")
        sb.appendLine("Среднее время выполнения (дней),${"%.1f".format(report.averageCompletionDays)},,")
        sb.appendLine("Просрочено на текущую дату,${report.overdueCount},,")
        sb.appendLine("Пропускная способность (задач/день),${"%.2f".format(report.throughput)},,")
        sb.appendLine("Эффективность (завершённые/созданные),${"%.1f%%".format(report.efficiency)},,")
        return sb.toString()
    }

    override fun exportToPdf(report: PeriodReport): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A4)
        PdfWriter.getInstance(document, outputStream)
        document.open()

        document.add(Paragraph("Отчёт по проекту", Font(Font.HELVETICA, 18f, Font.BOLD)))
        document.add(Paragraph("Период: ${report.from} – ${report.to}", Font(Font.HELVETICA, 12f)))
        document.add(Chunk.NEWLINE)

        val table = PdfPTable(4).apply {
            widthPercentage = 100f
            addCell(createHeaderCell("Дата"))
            addCell(createHeaderCell("Создано"))
            addCell(createHeaderCell("Завершено"))
            addCell(createHeaderCell("WIP (сред.)"))
        }

        val allDates = (report.dailyCreation.keys + report.dailyCompletion.keys).sortedBy { it }
        for (dateKey in allDates) {
            val created = report.dailyCreation[dateKey] ?: 0
            val completed = report.dailyCompletion[dateKey] ?: 0
            table.addCell(createBodyCell(dateKey))
            table.addCell(createBodyCell(created.toString()))
            table.addCell(createBodyCell(completed.toString()))
            table.addCell(createBodyCell("%.2f".format(report.wipAverage)))
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