package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.DiffSourceConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.diff.FileDiffSource
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.types.shouldBeInstanceOf
import org.jacoco.core.analysis.ICoverageNode.CounterEntity
import org.jacoco.report.check.Limit
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

class ReportsFactoryTest {

    @ParameterizedTest
    @CsvSource(
        "INSTRUCTION, INSTRUCTION, 0.1",
        "BRANCH, BRANCH, 0.2",
        "LINE, LINE, 0.3",
    )
    fun `should return delta coverage report with violation rule if min coverage is greater than zero`(
        entity: CoverageEntity,
        expectedEntity: CounterEntity,
        minCoverage: Double,
    ) {
        // GIVEN
        val deltaCoverageConfig: DeltaCoverageConfig = buildDeltaCoverageConfig(entity, minCoverage)

        // WHEN
        val actualReports = reportFactory(deltaCoverageConfig, FileDiffSource("any"))

        // THEN
        actualReports
            .shouldHaveSize(1)
            .first()
            .shouldBeInstanceOf<DiffReport>()
            .assertDiffReport(minCoverage, expectedEntity)
    }

    @ParameterizedTest
    @EnumSource(CoverageEntity::class)
    fun `should return delta coverage report without violation rule if min coverage is zero`(
        entity: CoverageEntity,
    ) {
        // GIVEN
        val deltaCoverageConfig: DeltaCoverageConfig = buildDeltaCoverageConfig(entity, 0.0)

        // WHEN
        val actualReports = reportFactory(deltaCoverageConfig, FileDiffSource("any"))

        // THEN
        actualReports
            .shouldHaveSize(1)
            .first()
            .shouldBeInstanceOf<DiffReport>()
            .violation.violationRules
            .shouldBeEmpty()
    }

    private fun DiffReport.assertDiffReport(
        expectedMinCoverage: Double,
        expectedEntity: CounterEntity
    ) {
        val limit: Limit = violation.violationRules
            .shouldHaveSize(1).first()
            .limits.shouldHaveSize(1).first()
        assertSoftly(limit) {
            minimum shouldBeEqualComparingTo expectedMinCoverage.toString()
            entity shouldBeEqualComparingTo expectedEntity
        }
    }

    private fun buildDeltaCoverageConfig(
        entity: CoverageEntity,
        minCoverage: Double
    ): DeltaCoverageConfig {
        return DeltaCoverageConfig {
            diffSourceConfig = DiffSourceConfig { file = "any" }
            coverageRulesConfig = CoverageRulesConfig {
                violationRules += ViolationRule {
                    coverageEntity = entity
                    minCoverageRatio = minCoverage
                }
            }
        }
    }
}
