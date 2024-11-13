package io.github.surpsg.deltacoverage.report.jacoco.verification

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.collections.shouldBeEmpty
import io.mockk.mockk
import org.jacoco.core.internal.analysis.MethodCoverageImpl
import org.jacoco.report.IReportVisitor
import org.jacoco.report.check.Limit
import org.jacoco.report.check.Rule
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CoverageRulesVisitorTest {

    @Nested
    inner class NoOpCoverageRulesVisitorTest {

        @Test
        fun `should return empty list of results`() {
            NoOpCoverageRulesVisitor.verificationResults.shouldBeEmpty()
        }
    }

    @Nested
    inner class DefaultCoverageRulesVisitorTest {

        @Test
        fun `should return empty verification results when no coverage`() {
            // GIVEN
            val violationsOutput = ViolationsOutputResolver(CoverageRulesConfig {})
            val rulesVisitor = DefaultCoverageRulesVisitor(
                violationsOutput,
                mockk<IReportVisitor>(),
            )

            // WHEN
            rulesVisitor.visitEnd()

            // THEN
            rulesVisitor.verificationResults.shouldBeEmpty()
        }

        @Test
        fun `should return violation when found violations`() {
            // GIVEN
            val violationMessage = "test violation"
            val violationsOutput = ViolationsOutputResolver(CoverageRulesConfig {}).apply {
                onViolation(METHOD_COVERAGE, Rule(), Limit(), violationMessage)
            }
            val rulesVisitor = DefaultCoverageRulesVisitor(
                violationsOutput,
                mockk<IReportVisitor>(),
            )

            // WHEN
            rulesVisitor.visitEnd()

            // THEN
            assertSoftly(rulesVisitor.verificationResults) {
                containExactly(
                    CoverageSummary.VerificationResult(
                        coverageEntity = CoverageEntity.INSTRUCTION,
                        violation = violationMessage,
                    )
                )
            }
        }
    }

    private companion object {
        val METHOD_COVERAGE = MethodCoverageImpl("testMethod", "", "()V")
    }
}
