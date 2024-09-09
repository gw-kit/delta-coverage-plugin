package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.intellij.coverage.IntellijRawCoverageDataProvider
import io.github.surpsg.deltacoverage.report.textual.TextualReportFacade
import io.github.surpsg.deltacoverage.report.textual.TextualReportFacade.BuildContext

internal class ConsoleReportBuilder(
    val view: String,
    val coverageRulesConfig: CoverageRulesConfig,
    val reportBound: ReportBound,
    val reporter: Reporter,
) : ReportBuilder {

    override fun buildReport() {
        if (reportBound == ReportBound.DELTA_REPORT) {
            val dataProvider = IntellijRawCoverageDataProvider(reporter.projectData)
            val buildContext = BuildContext {
                this.viewName = view
                reportType = ReportType.CONSOLE
                reportBound = this@ConsoleReportBuilder.reportBound
                coverageDataProvider = dataProvider
                outputStream = System.out
                shrinkLongClassName = true

                coverageRulesConfig.entitiesRules.forEach { (entity, coverage) ->
                    targetCoverage(entity, coverage.minCoverageRatio)
                }
            }
            TextualReportFacade.generateReport(buildContext)
        }
    }
}
