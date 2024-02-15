package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.report.ConsoleHtmlReportLinkRenderer
import io.github.surpsg.deltacoverage.report.FullReport
import io.github.surpsg.deltacoverage.report.JacocoReport
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.jacoco.csv.ConsoleCoverageReportOutputStream
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
    private val report: FullReport
) : AnalyzableReport {

    override fun buildVisitor(): IReportVisitor {
        return report.jacocoReports
            .mapNotNull { buildReportVisitor(it) }
            .let(::MultiReportVisitor)
    }

    private fun buildReportVisitor(it: JacocoReport): IReportVisitor? {
        val reportFile: File = report.resolveReportAbsolutePath(it)
        return when (it.reportType) {
            ReportType.XML -> reportFile.createFileOutputStream().let(XMLFormatter()::createVisitor)

            ReportType.CSV -> reportFile.createFileOutputStream().let(CSVFormatter()::createVisitor)

            ReportType.HTML -> {
                ConsoleHtmlReportLinkRenderer.render(reportBound, reportFile)
                FileMultiReportOutput(reportFile).let(HTMLFormatter()::createVisitor)
            }

            ReportType.CONSOLE -> {
                if (reportBound == ReportBound.FULL_REPORT) {
                    null
                } else {
                    ConsoleCoverageReportOutputStream(System.out).let(CSVFormatter()::createVisitor)
                }
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
