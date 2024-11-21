package io.github.surpsg.deltacoverage.report.intellij

import com.intellij.rt.coverage.data.ProjectData
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.DeltaReportGeneratorFacade
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.intellij.coverage.NamedReportLoadStrategy
import io.github.surpsg.deltacoverage.report.intellij.coverage.ReportLoadStrategyFactory
import io.github.surpsg.deltacoverage.report.intellij.report.CoverageReportFactory
import io.github.surpsg.deltacoverage.report.intellij.report.ReportBuilder
import io.github.surpsg.deltacoverage.report.intellij.verifier.CoverageAssertion

internal class IntellijDeltaReportGeneratorFacade : DeltaReportGeneratorFacade() {

    override fun generate(reportContext: ReportContext): CoverageSummary {
        val reportBoundToLoadStrategy: Map<ReportBound, NamedReportLoadStrategy> =
            ReportLoadStrategyFactory.buildReportLoadStrategies(reportContext)
                .associateBy { it.reportBound }

        CoverageReportFactory
            .reportBuildersBy(
                reportContext,
                reportBoundToLoadStrategy.values,
            )
            .forEach(ReportBuilder::buildReport)

        return verifyCoverage(reportContext, reportBoundToLoadStrategy)
    }

    private fun verifyCoverage(
        reportContext: ReportContext,
        reportBoundToLoadStrategy: Map<ReportBound, NamedReportLoadStrategy>,
    ): CoverageSummary {
        val value: NamedReportLoadStrategy = reportBoundToLoadStrategy.getValue(ReportBound.DELTA_REPORT)
        val projectData: ProjectData = value.reportLoadStrategy.projectData
        return CoverageAssertion.verify(
            view = reportContext.deltaCoverageConfig.view,
            projectData = projectData,
            coverageRulesConfig = reportContext.deltaCoverageConfig.coverageRulesConfig,
        )
    }
}
