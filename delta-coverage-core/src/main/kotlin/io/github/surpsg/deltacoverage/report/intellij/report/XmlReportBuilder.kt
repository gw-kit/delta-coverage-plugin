package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.config.ReportsConfig
import java.io.File

internal class XmlReportBuilder(
    reportsConfig: ReportsConfig,
    reportBound: ReportBound,
    reporter: Reporter,
) : ReportBuilder(reporter, reportBound, reportsConfig) {

    override fun buildReport() {
        val reportPath: File = ReportPathStrategy.Xml(reportsConfig).buildReportPath(reportBound)
        reporter.createXMLReport(reportPath)
    }
}

