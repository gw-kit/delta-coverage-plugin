package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.config.ReportsConfig
import java.io.File

internal class HtmlReportBuilder(
    private val reportName: String,
    reportsConfig: ReportsConfig,
    reportBound: ReportBound,
    reporter: Reporter,
) : ReportBuilder(
    reporter = reporter,
    reportBound = reportBound,
    reportsConfig = reportsConfig
) {

    override fun buildReport() {
        val reportPath: File = ReportPathStrategy.Html(reportsConfig).buildReportPath(reportBound)
        reporter.createHTMLReport(
            reportPath,
            reportName,
            null
        )
    }
}
