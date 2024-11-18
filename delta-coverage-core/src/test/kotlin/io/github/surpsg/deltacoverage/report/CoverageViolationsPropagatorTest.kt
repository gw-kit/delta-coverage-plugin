package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.exception.CoverageViolatedException
import io.github.surpsg.deltacoverage.report.CoverageSummary.Info
import io.github.surpsg.deltacoverage.report.CoverageSummary.VerificationResult
import io.github.surpsg.deltacoverage.report.CoverageViolationsPropagatorTest.CoverageSummaryBuilder.Companion.coverageSummary
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class CoverageViolationsPropagatorTest {
    private val propagator = CoverageViolationsPropagator()

    @Test
    fun `propagate should throw CoverageViolatedException if failOnViolation is true and there are violations`() {
        // GIVEN
        val coverageSummary = coverageSummary {
            coverageRulesConfig = CoverageRulesConfig {
                failOnViolation = true
            }
            verifications += VerificationResult(
                coverageEntity = CoverageEntity.INSTRUCTION,
                violation = "any violation 3"
            )
        }

        // WHEN // THEN
        shouldThrow<CoverageViolatedException> {
            propagator.propagateAll(coverageSummary)
        }
    }

    @Test
    fun `propagate should not throw any exception if failOnViolation is true and there are no violations`() {
        // GIVEN
        val coverageSummary = coverageSummary {
            coverageRulesConfig = CoverageRulesConfig {
                failOnViolation = true
            }
            verifications.clear()
        }

        // WHEN // THEN
        shouldNotThrow<CoverageViolatedException> {
            propagator.propagateAll(coverageSummary)
        }
    }

    @Test
    fun `propagate should not throw any exception if failOnViolation is false`() {
        // GIVEN
        val coverageSummary = coverageSummary {
            coverageRulesConfig = CoverageRulesConfig {
                failOnViolation = false
            }
            verifications += VerificationResult(
                coverageEntity = CoverageEntity.INSTRUCTION,
                violation = "any violation 3"
            )
        }

        // WHEN // THEN
        shouldNotThrow<CoverageViolatedException> {
            propagator.propagateAll(coverageSummary)
        }
    }

    private class CoverageSummaryBuilder {
        var view: String = "any view"
        var coverageRulesConfig: CoverageRulesConfig = CoverageRulesConfig {
            failOnViolation = false
        }
        var verifications: MutableList<VerificationResult> = mutableListOf(
            VerificationResult(
                coverageEntity = CoverageEntity.LINE,
                violation = "any violation 1"
            ),
            VerificationResult(
                coverageEntity = CoverageEntity.BRANCH,
                violation = "any violation 2"
            )
        )
        var coverageInfo: MutableList<Info> = mutableListOf(
            Info(
                coverageEntity = CoverageEntity.LINE,
                covered = 1,
                total = 2
            ),
            Info(
                coverageEntity = CoverageEntity.BRANCH,
                covered = 3,
                total = 4
            )
        )

        private fun build() = CoverageSummary(
            reportBound = ReportBound.DELTA_REPORT,
            view = view,
            coverageRulesConfig = coverageRulesConfig,
            verifications = verifications,
            coverageInfo = coverageInfo,
        )

        companion object {
            fun coverageSummary(
                customize: CoverageSummaryBuilder.() -> Unit = {}
            ): CoverageSummary {
                return CoverageSummaryBuilder().apply(customize).build()
            }
        }
    }
}
