package io.github.surpsg.deltacoverage.report.jacoco.report

import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.jacoco.verification.CoverageRulesVisitor
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor

internal class VerifiableReportVisitor private constructor(
    val reportBound: ReportBound,
    allVisitors: IReportVisitor,
    private val coverageRulesVisitor: CoverageRulesVisitor,
    private val coverageInfoVisitor: CoverageInfoVisitor,
) : IReportVisitor by allVisitors {

    val verificationResults: List<CoverageSummary.VerificationResult>
        get() = coverageRulesVisitor.verificationResults

    val coverageInfo: List<CoverageSummary.Info>
        get() = coverageInfoVisitor.coverageSummary

    companion object {

        fun create(
            reportBound: ReportBound,
            reportVisitors: Iterable<IReportVisitor>,
            coverageRulesVisitor: CoverageRulesVisitor,
            coverageInfoVisitor: CoverageInfoVisitor,
        ): VerifiableReportVisitor {
            val allVisitors: MutableList<IReportVisitor> = reportVisitors.toMutableList().apply {
                add(coverageRulesVisitor)
                add(coverageInfoVisitor)
            }
            return VerifiableReportVisitor(
                reportBound = reportBound,
                allVisitors = MultiReportVisitor(allVisitors),
                coverageRulesVisitor = coverageRulesVisitor,
                coverageInfoVisitor = coverageInfoVisitor,
            )
        }
    }
}
