package io.github.surpsg.deltacoverage.report.jacoco.report

import io.github.surpsg.deltacoverage.report.CoverageVerificationResult
import io.github.surpsg.deltacoverage.report.jacoco.verification.CoverageRulesVisitor
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor

internal class VerifiableReportVisitor private constructor(
    allVisitors: IReportVisitor,
    private val coverageRulesVisitor: CoverageRulesVisitor,
) : IReportVisitor by allVisitors {

    val verificationResults: List<CoverageVerificationResult>
        get() = coverageRulesVisitor.verificationResults

    companion object {

        fun create(
            reportVisitors: Iterable<IReportVisitor>,
            coverageRulesVisitor: CoverageRulesVisitor,
        ): VerifiableReportVisitor {
            val allVisitors = reportVisitors.toMutableList()
            allVisitors += coverageRulesVisitor

            return VerifiableReportVisitor(
                MultiReportVisitor(allVisitors),
                coverageRulesVisitor,
            )
        }
    }
}
