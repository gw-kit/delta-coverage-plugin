package io.github.surpsg.deltacoverage.report.intellij

import com.intellij.rt.coverage.data.ProjectData
import io.github.surpsg.deltacoverage.report.DeltaReportGeneratorFacade
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.intellij.coverage.NamedReportLoadStrategy
import io.github.surpsg.deltacoverage.report.intellij.coverage.ReportLoadStrategyFactory
import io.github.surpsg.deltacoverage.report.intellij.report.CoverageReportFactory
import io.github.surpsg.deltacoverage.report.intellij.report.ReportBuilder
import io.github.surpsg.deltacoverage.report.intellij.verifier.CoverageAssertion

internal class IntellijDeltaReportGeneratorFacade(
    reportContext: ReportContext
) : DeltaReportGeneratorFacade(reportContext) {

    override fun generateReport(): DeltaReportGeneratorFacade {
        generate()
        return this
    }

    private fun generate() {
        val reportBoundToLoadStrategy: Map<ReportBound, NamedReportLoadStrategy> =
            ReportLoadStrategyFactory.buildReportLoadStrategies(reportContext)
                .associateBy { it.reportBound }

        CoverageReportFactory
            .reportBuildersBy(
                reportContext.deltaCoverageConfig.reportsConfig,
                reportBoundToLoadStrategy.values
            )
            .forEach(ReportBuilder::buildReport)

        verifyCoverage(reportBoundToLoadStrategy)
    }

    private fun verifyCoverage(reportBoundToLoadStrategy: Map<ReportBound, NamedReportLoadStrategy>) {
        val projectData: ProjectData = reportBoundToLoadStrategy.getValue(ReportBound.DELTA_REPORT)
            .reportLoadStrategy.projectData
        CoverageAssertion.verify(
            projectData,
            reportContext.deltaCoverageConfig.coverageRulesConfig
        )
    }
}
