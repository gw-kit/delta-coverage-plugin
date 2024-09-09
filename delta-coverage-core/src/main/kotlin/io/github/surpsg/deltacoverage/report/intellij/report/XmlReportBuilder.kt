package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.path.ReportPathStrategy
import java.io.File

internal class XmlReportBuilder(
    val reportBound: ReportBound,
    private val reportsConfig: ReportsConfig,
    private val reporter: Reporter,
) : ReportBuilder {

    override fun buildReport() {
        val reportPath: File = ReportPathStrategy.Xml(reportsConfig).buildReportPath(reportBound)
        reporter.createXMLReport(reportPath)
    }
}
