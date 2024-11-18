package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.EnabledReportTypeFactory
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.jacoco.report.JacocoReport
import io.github.surpsg.deltacoverage.report.jacoco.report.VerifiableReportVisitor
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore

internal interface AnalyzableReport {

    val reportBound: ReportBound

    fun buildVisitor(): VerifiableReportVisitor

    fun buildAnalyzer(executionDataStore: ExecutionDataStore, coverageVisitor: ICoverageVisitor): Analyzer
}

internal fun analyzableReportFactory(
    reportContext: ReportContext
): Set<AnalyzableReport> {
    val allReports = mutableSetOf<AnalyzableReport>()

    val reportsConfig: ReportsConfig = reportContext.deltaCoverageConfig.reportsConfig
    val enabledReportTypes: Set<ReportType> = EnabledReportTypeFactory.obtain(reportsConfig)

    allReports += DeltaCoverageAnalyzableReport(
        reportContext,
        ReportBound.DELTA_REPORT.buildJacocoReports(reportContext, enabledReportTypes),
    )

    if (reportsConfig.fullCoverageReport) {
        allReports += FullCoverageAnalyzableReport(
            ReportBound.FULL_REPORT.buildJacocoReports(reportContext, enabledReportTypes)
        )
    }

    return allReports
}

private fun ReportBound.buildJacocoReports(
    reportContext: ReportContext,
    reportTypes: Set<ReportType>
): List<JacocoReport> {
    return reportTypes.map { reportType ->
        JacocoReport(
            reportType,
            this,
            reportContext.deltaCoverageConfig.reportsConfig,
            reportContext.deltaCoverageConfig.coverageRulesConfig,
        )
    }
}
