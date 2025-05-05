package io.github.surpsg.deltacoverage.report.intellij

import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
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

    override fun generate(reportContext: ReportContext): Map<ReportBound, CoverageSummary> {
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
    ): Map<ReportBound, CoverageSummary> = reportBoundToLoadStrategy.mapValues { (reportBound, strategy) ->
        val coverageRules = when (reportBound) {
            ReportBound.FULL_REPORT -> CoverageRulesConfig()
            ReportBound.DELTA_REPORT -> reportContext.deltaCoverageConfig.coverageRulesConfig
        }
        CoverageAssertion.verify(
            view = reportContext.deltaCoverageConfig.view,
            projectData = strategy.reportLoadStrategy.projectData,
            coverageRulesConfig = coverageRules,
        )
    }
}
