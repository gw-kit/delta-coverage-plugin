package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.report.EnabledReportTypeFactory
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.intellij.coverage.NamedReportLoadStrategy

internal object CoverageReportFactory {

    fun reportBuildersBy(
        reportsContext: ReportContext,
        reportLoadStrategies: Iterable<NamedReportLoadStrategy>,
    ): Sequence<ReportBuilder> {
        val enabledReports: Iterable<ReportType> = EnabledReportTypeFactory.obtain(
            reportsContext.deltaCoverageConfig.reportsConfig
        )
        return reportLoadStrategies.asSequence()
            .flatMap { loadStrategy ->
                buildReportBuilders(reportsContext, loadStrategy, enabledReports)
            }
    }

    private fun buildReportBuilders(
        reportsContext: ReportContext,
        namedReportLoadStrategy: NamedReportLoadStrategy,
        enabledReports: Iterable<ReportType>,
    ): Sequence<ReportBuilder> {
        return enabledReports.asSequence()
            .sortedBy { it.priority }
            .map { reportType ->
                reportType.buildReportBuilder(namedReportLoadStrategy, reportsContext)
            }
    }

    private fun ReportType.buildReportBuilder(
        reportLoadStrategy: NamedReportLoadStrategy,
        reportsContext: ReportContext,
    ): ReportBuilder {
        val reportsConfig = reportsContext.deltaCoverageConfig.reportsConfig
        val reporter = Reporter(reportLoadStrategy.reportLoadStrategy)
        return when (this) {
            ReportType.HTML -> HtmlReportBuilder(
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
                view = reportsConfig.view,
                reportBound = reportLoadStrategy.reportBound,
                reporter = reporter,
                coverageRulesConfig = reportsContext.deltaCoverageConfig.coverageRulesConfig,
            )

            ReportType.MARKDOWN -> MarkdownReportBuilder(
                reportView = reportsConfig.view,
                reportBound = reportLoadStrategy.reportBound,
                reportContext = reportsContext,
                reporter = reporter,
            )
        }
    }
}
