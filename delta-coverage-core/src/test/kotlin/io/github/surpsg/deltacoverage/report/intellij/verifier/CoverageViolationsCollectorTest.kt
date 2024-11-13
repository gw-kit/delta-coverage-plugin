package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.verify.Verifier
import com.intellij.rt.coverage.verify.api.ValueType
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class CoverageViolationsCollectorTest {

    @ParameterizedTest
    @EnumSource
    fun `should not build violations if coverage rule is satisfied`(
        coverageEntity: CoverageEntity,
    ) {
        // GIVEN
        val minCoverage = 0.8
        val missedCount = 2L
        val coveredCount = 8L
        val collectedCoverage = coverageEntity.buildCollectedCoverage {
            covered = coveredCount
            missed = missedCount
        }
        val coverageViolationsCollector: CoverageViolationsCollector =
            coverageEntity.buildCoverageViolationsCollector(minCoverage = minCoverage)

        // WHEN
        coverageViolationsCollector.consume("any", collectedCoverage)

        // THEN
        coverageViolationsCollector.violations.shouldBeEmpty()
    }

    @ParameterizedTest
    @EnumSource
    fun `should build violations`(
        coverageEntity: CoverageEntity,
    ) {
        // GIVEN
        val minCoverage = 0.8
        val missedCount = 8L
        val coveredCount = 2L
        val collectedCoverage = coverageEntity.buildCollectedCoverage {
            covered = coveredCount
            missed = missedCount
        }
        val coverageViolationsCollector: CoverageViolationsCollector =
            coverageEntity.buildCoverageViolationsCollector(minCoverage = minCoverage)

        // WHEN
        coverageViolationsCollector.consume("any", collectedCoverage)

        // THEN
        coverageViolationsCollector.violations shouldContain CoverageVerifier.Violation(
            coverageEntity = coverageEntity,
            expectedMinValue = minCoverage,
            actualValue = 0.2
        )
    }

    @Test
    fun `should not build violation if neither missed nor covered data`() {
        // GIVEN
        val coverageEntity = CoverageEntity.LINE
        val collectedCoverage = coverageEntity.buildCollectedCoverage {
            covered = 0
            missed = 0
        }
        val coverageViolationsCollector: CoverageViolationsCollector =
            coverageEntity.buildCoverageViolationsCollector(minCoverage = 0.999)

        // WHEN
        coverageViolationsCollector.consume("any", collectedCoverage)

        // THEN
        coverageViolationsCollector.violations.shouldBeEmpty()
    }

    @ParameterizedTest
    @EnumSource
    fun `should build violations if threshold is met`(
        coverageEntity: CoverageEntity,
    ) {
        // GIVEN
        val minCoverage = 0.7
        val missedCount = 70L
        val coveredCount = 30L
        val threshold = 10

        val collectedCoverage = coverageEntity.buildCollectedCoverage {
            covered = coveredCount
            missed = missedCount
        }
        val coverageViolationsCollector: CoverageViolationsCollector =
            coverageEntity.buildCoverageViolationsCollector(minCoverage = minCoverage, threshold = threshold)

        // WHEN
        coverageViolationsCollector.consume("any", collectedCoverage)

        // THEN
        coverageViolationsCollector.violations shouldContain CoverageVerifier.Violation(
            coverageEntity = coverageEntity,
            expectedMinValue = minCoverage,
            actualValue = 0.3
        )
    }

    @ParameterizedTest
    @EnumSource
    fun `should ignore violations if threshold is not reached`(
        coverageEntity: CoverageEntity,
    ) {
        // GIVEN
        val totalCount = 9L
        val threshold = 10
        val collectedCoverage = coverageEntity.buildCollectedCoverage {
            covered = 0
            missed = totalCount
        }
        val coverageViolationsCollector: CoverageViolationsCollector =
            coverageEntity.buildCoverageViolationsCollector(minCoverage = 1.0, threshold = threshold)

        // WHEN
        coverageViolationsCollector.consume("any", collectedCoverage)

        // THEN
        coverageViolationsCollector.violations.shouldBeEmpty()
    }

    private fun CoverageEntity.buildCoverageViolationsCollector(
        minCoverage: Double = 1.0,
        threshold: Int? = null,
    ) = CoverageViolationsCollector(
        CoverageRuleWithThreshold(
            id = 1,
            coverageEntity = this,
            valueType = ValueType.COVERED_RATE,
            min = minCoverage.toBigDecimal(),
            threshold = threshold
        )
    )

    private fun CoverageEntity.buildCollectedCoverage(
        customizer: Verifier.CollectedCoverage.Counter.() -> Unit = {}
    ): Verifier.CollectedCoverage = Verifier.CollectedCoverage().also {
        when (this) {
            CoverageEntity.INSTRUCTION -> it.instructionCounter.customizer()
            CoverageEntity.BRANCH -> it.branchCounter.customizer()
            CoverageEntity.LINE -> it.lineCounter.customizer()
        }
    }
}
