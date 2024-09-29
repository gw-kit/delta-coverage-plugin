package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.ReportsConfig

object EnabledReportTypeFactory {

    fun obtain(reportsConfig: ReportsConfig): Set<ReportType> =
        ReportType.entries.asSequence()
            .map { reportType ->
                when (reportType) {
                    ReportType.HTML -> ReportType.HTML to reportsConfig.html.enabled
                    ReportType.XML -> ReportType.XML to reportsConfig.xml.enabled
                    ReportType.CONSOLE -> ReportType.CONSOLE to reportsConfig.console.enabled
                    ReportType.MARKDOWN -> ReportType.MARKDOWN to reportsConfig.markdown.enabled
                }
            }
            .filter { (_, enabled) -> enabled }
            .map { (reportType, _) -> reportType }
            .toSet()
}
