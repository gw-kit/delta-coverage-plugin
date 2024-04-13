package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.intellij.coverage.IntellijRawCoverageDataProvider
import io.github.surpsg.deltacoverage.report.textual.TextualReportFacade
import io.github.surpsg.deltacoverage.report.textual.TextualReportFacade.BuildContext
import java.io.File

internal class MarkdownReportBuilder(
    val reportBound: ReportBound,
    private val reportsConfig: ReportsConfig,
    private val reporter: Reporter,
) : ReportBuilder {

    override fun buildReport() {
        val reportPath: File = ReportPathStrategy.Markdown(reportsConfig).buildReportPath(reportBound)
        reportPath.outputStream().use { outputStream ->
            val buildContext = BuildContext(
                reportType = ReportType.MARKDOWN,
                coverageDataProvider = IntellijRawCoverageDataProvider(reporter.projectData),
                outputStream = outputStream,
            )
            TextualReportFacade.generateReport(buildContext)
        }
    }
}
