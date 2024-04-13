package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.EnabledReportTypeFactory
import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.intellij.coverage.NamedReportLoadStrategy

internal object CoverageReportFactory {

    fun reportBuildersBy(
        reportsConfig: ReportsConfig,
        reportLoadStrategies: Iterable<NamedReportLoadStrategy>,
    ): Sequence<ReportBuilder> {
        val enabledReports: Iterable<ReportType> = EnabledReportTypeFactory.obtain(reportsConfig)
        return reportLoadStrategies.asSequence()
            .flatMap { loadStrategy ->
                buildReportBuilders(reportsConfig, loadStrategy, enabledReports)
            }
    }

    private fun buildReportBuilders(
        reportsConfig: ReportsConfig,
        namedReportLoadStrategy: NamedReportLoadStrategy,
        enabledReports: Iterable<ReportType>,
    ): Sequence<ReportBuilder> {
        return enabledReports.asSequence()
            .sortedBy { it.priority }
            .map { reportType ->
                reportType.buildReportBuilder(namedReportLoadStrategy, reportsConfig)
            }
    }

    private fun ReportType.buildReportBuilder(
        reportLoadStrategy: NamedReportLoadStrategy,
        reportsConfig: ReportsConfig,
    ): ReportBuilder {
        val reporter = Reporter(reportLoadStrategy.reportLoadStrategy)
        return when (this) {
            ReportType.HTML -> HtmlReportBuilder(
                reportName = reportLoadStrategy.reportName,
                reportBound = reportLoadStrategy.reportBound,
                reportsConfig = reportsConfig,
                reporter = reporter,
            )

            ReportType.XML -> XmlReportBuilder(
                reportBound = reportLoadStrategy.reportBound,
                reportsConfig = reportsConfig,
                reporter = reporter,
            )

            ReportType.CONSOLE -> ConsoleReportBuilder(
                reportBound = reportLoadStrategy.reportBound,
                reporter = reporter,
            )

            ReportType.MARKDOWN -> MarkdownReportBuilder(
                reportBound = reportLoadStrategy.reportBound,
                reportsConfig = reportsConfig,
                reporter = reporter,
            )

            ReportType.CSV -> error("Unsupported report type: $this")
        }
    }
}
