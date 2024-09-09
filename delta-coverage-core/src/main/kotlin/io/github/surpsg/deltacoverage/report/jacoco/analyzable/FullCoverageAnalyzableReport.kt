package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.jacoco.report.JacocoReport
import io.github.surpsg.deltacoverage.report.jacoco.report.ReportVisitorFactory
import io.github.surpsg.deltacoverage.report.jacoco.report.VerifiableReportVisitor
import io.github.surpsg.deltacoverage.report.jacoco.verification.NoOpCoverageRulesVisitor
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.report.IReportVisitor

internal open class FullCoverageAnalyzableReport(
    private val report: FullReport,
    private val coverageRulesConfig: CoverageRulesConfig,
    private val jacocoReports: List<JacocoReport>
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

    override fun buildVisitor(): VerifiableReportVisitor {
        return VerifiableReportVisitor.create(
            reportVisitors(),
            NoOpCoverageRulesVisitor,
        )
    }

    override fun buildAnalyzer(
        executionDataStore: ExecutionDataStore,
        coverageVisitor: ICoverageVisitor
    ): Analyzer {
        return Analyzer(executionDataStore, coverageVisitor)
    }

    fun reportVisitors(): List<IReportVisitor> {
        return jacocoReports
            .sortedBy { it.reportType.priority }
            .mapNotNull { jacocoReport -> ReportVisitorFactory.buildVisitor(jacocoReport) }
    }

    open val reportBound: ReportBound = ReportBound.FULL_REPORT

}
