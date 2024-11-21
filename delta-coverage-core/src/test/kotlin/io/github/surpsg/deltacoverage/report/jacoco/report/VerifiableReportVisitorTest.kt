package io.github.surpsg.deltacoverage.report.jacoco.report

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.jacoco.verification.CoverageRulesVisitor
import io.github.surpsg.deltacoverage.report.jacoco.verification.NoOpCoverageRulesVisitor
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

class VerifiableReportVisitorTest {

    @Test
    fun `should return non empty verification result when found violations`() {
        // GIVEN
        val expectedVerificationResult = CoverageSummary.VerificationResult(
            coverageEntity = CoverageEntity.INSTRUCTION,
            violation = "found some violation test",
        )
        val reportVisitor = VerifiableReportVisitor.create(
            reportBound = ReportBound.FULL_REPORT,
            reportVisitors = emptyList(),
            coverageRulesVisitor = object : CoverageRulesVisitor by NoOpCoverageRulesVisitor {
                override val verificationResults: List<CoverageSummary.VerificationResult> =
                    listOf(expectedVerificationResult)
            },
            coverageInfoVisitor = CoverageInfoVisitor.NO_OP_VISITOR,
        )

        // WHEN
        val actualResults: List<CoverageSummary.VerificationResult> = reportVisitor.verificationResults

        // THEN
        actualResults.shouldContainExactly(expectedVerificationResult)
    }

    @Test
    fun `should return coverage info`() {
        // GIVEN
        val expectedInfo = CoverageSummary.Info(
            coverageEntity = CoverageEntity.INSTRUCTION,
            covered = 1,
            total = 2,
        )
        val reportVisitor = VerifiableReportVisitor.create(
            reportBound = ReportBound.FULL_REPORT,
            reportVisitors = emptyList(),
            coverageRulesVisitor = NoOpCoverageRulesVisitor,
            coverageInfoVisitor = object : CoverageInfoVisitor() {
                override val coverageSummary: List<CoverageSummary.Info> = listOf(expectedInfo)
            },
        )

        // WHEN
        val actualResults: List<CoverageSummary.Info> = reportVisitor.coverageInfo

        // THEN
        actualResults.shouldContainExactly(expectedInfo)
    }
}
