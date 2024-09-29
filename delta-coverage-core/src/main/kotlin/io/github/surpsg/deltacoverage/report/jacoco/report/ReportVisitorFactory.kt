package io.github.surpsg.deltacoverage.report.jacoco.report

import io.github.surpsg.deltacoverage.report.ConsoleHtmlReportLinkRenderer
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.jacoco.csv.TextualReportOutputStream
import org.jacoco.report.FileMultiReportOutput
import org.jacoco.report.IReportVisitor
import org.jacoco.report.csv.CSVFormatter
import org.jacoco.report.html.HTMLFormatter
import org.jacoco.report.xml.XMLFormatter
import java.io.File
import java.io.FileOutputStream

internal object ReportVisitorFactory {

    fun buildVisitor(
        jacocoReport: JacocoReport,
    ): IReportVisitor? {
        val reportFile: File = ReportPathFactory.resolveReportAbsolutePath(jacocoReport)
        return when (jacocoReport.reportType) {
            ReportType.XML -> reportFile.createFileOutputStream().let(XMLFormatter()::createVisitor)

            ReportType.HTML -> buildHtmReportVisitor(jacocoReport, reportFile)

            ReportType.MARKDOWN -> TextualReportOutputStream(
                jacocoReport.reportsConfig.view,
                jacocoReport.reportType,
                jacocoReport.reportBound,
                jacocoReport.coverageRulesConfig,
                reportFile.createFileOutputStream(),
            ).let(CSVFormatter()::createVisitor)

            ReportType.CONSOLE -> {
                buildConsoleReportVisitor(jacocoReport)
            }
        }
    }

    private fun buildConsoleReportVisitor(
        jacocoReport: JacocoReport,
    ): IReportVisitor? {
        return if (jacocoReport.reportBound == ReportBound.FULL_REPORT) {
            null
        } else {
            TextualReportOutputStream(
                jacocoReport.reportsConfig.view,
                jacocoReport.reportType,
                jacocoReport.reportBound,
                jacocoReport.coverageRulesConfig,
                System.out
            ).let(CSVFormatter()::createVisitor)
        }
    }

    private fun buildHtmReportVisitor(
        jacocoReport: JacocoReport,
        reportFile: File,
    ): IReportVisitor {
        val htmlReporter: IReportVisitor = FileMultiReportOutput(reportFile).let(HTMLFormatter()::createVisitor)
        return object : IReportVisitor by htmlReporter {
            override fun visitEnd() {
                htmlReporter.visitEnd()
                ConsoleHtmlReportLinkRenderer.render(
                    jacocoReport.reportsConfig.view,
                    jacocoReport.reportBound,
                    reportFile,
                )
            }
        }
    }

    private fun File.createFileOutputStream(): FileOutputStream {
        parentFile.mkdirs()
        return FileOutputStream(this)
    }
}
