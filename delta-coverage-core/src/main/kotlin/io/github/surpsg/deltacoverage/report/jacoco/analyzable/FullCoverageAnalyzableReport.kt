package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.report.ConsoleHtmlReportLinkRenderer
import io.github.surpsg.deltacoverage.report.FullReport
import io.github.surpsg.deltacoverage.report.JacocoReport
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.jacoco.csv.TextualReportOutputStream
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.report.FileMultiReportOutput
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.csv.CSVFormatter
import org.jacoco.report.html.HTMLFormatter
import org.jacoco.report.xml.XMLFormatter
import java.io.File
import java.io.FileOutputStream

internal open class FullCoverageAnalyzableReport(
    private val report: FullReport,
    private val coverageRulesConfig: CoverageRulesConfig,
) : AnalyzableReport {

    override fun buildVisitor(): IReportVisitor {
        return report.jacocoReports
            .sortedBy { it.reportType.priority }
            .mapNotNull { buildReportVisitor(it) }
            .let(::MultiReportVisitor)
    }

    private fun buildReportVisitor(jacocoReport: JacocoReport): IReportVisitor? {
        val reportFile: File = report.resolveReportAbsolutePath(jacocoReport)
        return when (jacocoReport.reportType) {
            ReportType.XML -> reportFile.createFileOutputStream().let(XMLFormatter()::createVisitor)

            ReportType.CSV -> reportFile.createFileOutputStream().let(CSVFormatter()::createVisitor)

            ReportType.HTML -> buildHtmReportVisitor(reportFile)

            ReportType.MARKDOWN -> TextualReportOutputStream(
                jacocoReport.reportType,
                jacocoReport.reportBound,
                coverageRulesConfig,
                reportFile.createFileOutputStream()
            ).let(CSVFormatter()::createVisitor)

            ReportType.CONSOLE -> {
                if (reportBound == ReportBound.FULL_REPORT) {
                    null
                } else {
                    TextualReportOutputStream(
                        jacocoReport.reportType,
                        jacocoReport.reportBound,
                        coverageRulesConfig,
                        System.out,
                    ).let(CSVFormatter()::createVisitor)
                }
            }
        }
    }

    private fun buildHtmReportVisitor(reportFile: File): IReportVisitor {
        val htmlReporter: IReportVisitor = FileMultiReportOutput(reportFile).let(HTMLFormatter()::createVisitor)
        return object : IReportVisitor by htmlReporter {
            override fun visitEnd() {
                htmlReporter.visitEnd()
                ConsoleHtmlReportLinkRenderer.render(reportBound, reportFile)
            }
        }
    }

    private fun File.createFileOutputStream(): FileOutputStream {
        parentFile.mkdirs()
        return FileOutputStream(this)
    }

    override fun buildAnalyzer(
        executionDataStore: ExecutionDataStore,
        coverageVisitor: ICoverageVisitor
    ): Analyzer {
        return Analyzer(executionDataStore, coverageVisitor)
    }

    open val reportBound: ReportBound = ReportBound.FULL_REPORT

}
