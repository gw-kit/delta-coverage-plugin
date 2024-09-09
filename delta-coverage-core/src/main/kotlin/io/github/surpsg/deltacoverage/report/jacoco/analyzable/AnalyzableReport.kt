package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.jacoco.report.VerifiableReportVisitor
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore

internal interface AnalyzableReport {

    fun buildVisitor(): VerifiableReportVisitor
    fun buildAnalyzer(executionDataStore: ExecutionDataStore, coverageVisitor: ICoverageVisitor): Analyzer
}

internal fun analyzableReportFactory(
    reportContext: ReportContext
): Set<AnalyzableReport> {
    return reportFactory(reportContext)
        .map { reportMode ->
            when (reportMode) {
                is JacocoDeltaReport -> DeltaCoverageAnalyzableReport(
                    reportContext.deltaCoverageConfig.coverageRulesConfig,
                    reportMode,
                )

                else -> FullCoverageAnalyzableReport(
                    reportMode,
                    reportContext.deltaCoverageConfig.coverageRulesConfig,
                )
            }
        }.toSet()
}
