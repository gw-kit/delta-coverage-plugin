package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.ConsoleHtmlReportLinkRenderer
import io.github.surpsg.deltacoverage.report.ReportBound
import java.io.File

internal class HtmlReportBuilder(
    val reportName: String,
    val reportBound: ReportBound,
    private val reportsConfig: ReportsConfig,
    private val reporter: Reporter,
) : ReportBuilder {

    override fun buildReport() {
        val reportPath: File = ReportPathStrategy.Html(reportsConfig).buildReportPath(reportBound)
        ConsoleHtmlReportLinkRenderer.render(reportBound, reportPath)

        reporter.createHTMLReport(
            reportPath,
            reportName,
            null
        )
    }
}
