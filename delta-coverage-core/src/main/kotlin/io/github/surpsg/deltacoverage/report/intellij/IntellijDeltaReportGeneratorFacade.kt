package io.github.surpsg.deltacoverage.report.intellij

import com.intellij.rt.coverage.data.ProjectData
import io.github.surpsg.deltacoverage.report.CoverageVerificationResult
import io.github.surpsg.deltacoverage.report.DeltaReportGeneratorFacade
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.intellij.coverage.NamedReportLoadStrategy
import io.github.surpsg.deltacoverage.report.intellij.coverage.ReportLoadStrategyFactory
import io.github.surpsg.deltacoverage.report.intellij.report.CoverageReportFactory
import io.github.surpsg.deltacoverage.report.intellij.report.ReportBuilder
import io.github.surpsg.deltacoverage.report.intellij.verifier.CoverageAssertion

internal class IntellijDeltaReportGeneratorFacade : DeltaReportGeneratorFacade() {

    override fun generate(reportContext: ReportContext): List<CoverageVerificationResult> {
        val reportBoundToLoadStrategy: Map<ReportBound, NamedReportLoadStrategy> =
            ReportLoadStrategyFactory.buildReportLoadStrategies(reportContext)
                .associateBy { it.reportBound }

        CoverageReportFactory
            .reportBuildersBy(
                reportContext.deltaCoverageConfig.reportsConfig,
                reportBoundToLoadStrategy.values
            )
            .forEach(ReportBuilder::buildReport)

        return listOf(
            verifyCoverage(reportContext, reportBoundToLoadStrategy)
        )
    }

    private fun verifyCoverage(
        reportContext: ReportContext,
        reportBoundToLoadStrategy: Map<ReportBound, NamedReportLoadStrategy>,
    ): CoverageVerificationResult {
        val projectData: ProjectData = reportBoundToLoadStrategy.getValue(ReportBound.DELTA_REPORT)
            .reportLoadStrategy.projectData
        return CoverageAssertion.verify(
            reportContext.deltaCoverageConfig.view,
            projectData,
            reportContext.deltaCoverageConfig.coverageRulesConfig
        )
    }
}
