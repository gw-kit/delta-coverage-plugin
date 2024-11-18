package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.jacoco.report.CoverageInfoVisitor
import io.github.surpsg.deltacoverage.report.jacoco.report.JacocoReport
import io.github.surpsg.deltacoverage.report.jacoco.report.ReportVisitorFactory
import io.github.surpsg.deltacoverage.report.jacoco.report.VerifiableReportVisitor
import io.github.surpsg.deltacoverage.report.jacoco.verification.NoOpCoverageRulesVisitor
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.report.IReportVisitor

internal open class FullCoverageAnalyzableReport(
    private val jacocoReports: List<JacocoReport>
) : AnalyzableReport {

    override val reportBound: ReportBound = ReportBound.FULL_REPORT

    override fun buildVisitor(): VerifiableReportVisitor {
        return VerifiableReportVisitor.create(
            ReportBound.FULL_REPORT,
            reportVisitors(),
            NoOpCoverageRulesVisitor,
            CoverageInfoVisitor.NO_OP_VISITOR,
        )
    }

    override fun buildAnalyzer(
        executionDataStore: ExecutionDataStore,
        coverageVisitor: ICoverageVisitor
    ): Analyzer {
        return Analyzer(executionDataStore, coverageVisitor)
    }

    fun reportVisitors(): List<IReportVisitor> {
        return jacocoReports
            .sortedBy { it.reportType.priority }
            .mapNotNull { jacocoReport -> ReportVisitorFactory.buildVisitor(jacocoReport) }
    }
}
