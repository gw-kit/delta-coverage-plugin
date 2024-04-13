package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.light.ConsoleReportFacade
import io.github.surpsg.deltacoverage.report.intellij.coverage.IntellijRawCoverageDataProvider

internal class ConsoleReportBuilder(
    val reportBound: ReportBound,
    private val reporter: Reporter,
) : ReportBuilder {

    override fun buildReport() {
        if (reportBound == ReportBound.DELTA_REPORT) {
            val coverageDataProvider = IntellijRawCoverageDataProvider(reporter.projectData)
            ConsoleReportFacade.generateReport(coverageDataProvider, System.out)
        }
    }
}
