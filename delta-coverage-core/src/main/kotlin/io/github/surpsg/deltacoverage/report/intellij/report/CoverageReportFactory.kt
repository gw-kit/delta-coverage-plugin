package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.intellij.coverage.NamedReportLoadStrategy

internal object CoverageReportFactory {

    fun reportBuildersBy(
        reportsConfig: ReportsConfig,
        reportLoadStrategies: Iterable<NamedReportLoadStrategy>,
    ): Sequence<ReportBuilder> {
        val enabledReports: List<ReportType> = obtainEnabledReportTypes(reportsConfig)
        return reportLoadStrategies.asSequence()
            .flatMap { loadStrategy ->
                buildReportBuilders(reportsConfig, loadStrategy, enabledReports)
            }
    }

    private fun buildReportBuilders(
        reportsConfig: ReportsConfig,
        namedReportLoadStrategy: NamedReportLoadStrategy,
        enabledReports: List<ReportType>,
    ): Sequence<ReportBuilder> {
        return enabledReports.asSequence().map { reportType ->
            reportType.buildReportBuilder(namedReportLoadStrategy, reportsConfig)
        }
    }

    private fun obtainEnabledReportTypes(reportsConfig: ReportsConfig): List<ReportType> =
        ReportType.entries.asSequence()
            .map { reportType ->
                when (reportType) {
                    ReportType.HTML -> ReportType.HTML to reportsConfig.html.enabled
                    ReportType.XML -> ReportType.XML to reportsConfig.xml.enabled
                    ReportType.CONSOLE -> ReportType.CONSOLE to reportsConfig.console.enabled
                    ReportType.CSV -> ReportType.CSV to reportsConfig.csv.enabled
                }
            }
            .filter { (_, enabled) -> enabled }
            .map { (reportType, _) -> reportType }
            .toList()

    private fun ReportType.buildReportBuilder(
        reportLoadStrategy: NamedReportLoadStrategy,
        reportsConfig: ReportsConfig,
    ): ReportBuilder {
        val reporter = Reporter(reportLoadStrategy.reportLoadStrategy)
        return when (this) {
            ReportType.HTML -> HtmlReportBuilder(
                reportLoadStrategy.reportName,
                reportLoadStrategy.reportBound,
                reportsConfig,
                reporter,
            )

            ReportType.XML -> XmlReportBuilder(
                reportLoadStrategy.reportBound,
                reportsConfig,
                reporter,
            )

            ReportType.CONSOLE -> ConsoleReportBuilder(reportLoadStrategy.reportBound, reporter)

            ReportType.CSV -> error("Unsupported report type: $this")
        }
    }
}
