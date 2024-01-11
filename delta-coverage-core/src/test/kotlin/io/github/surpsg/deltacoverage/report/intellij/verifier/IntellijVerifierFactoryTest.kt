package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.verify.api.ValueType
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class IntellijVerifierFactoryTest {

    @ParameterizedTest
    @EnumSource
    fun `buildVerifiers should return verifier if coverage ratio is set`(
        entity: CoverageEntity
    ) {
        // GIVEN
        val expectedMinCoverage = 0.3
        val expectedThreshold = 111
        val config = CoverageRulesConfig {
            violationRules += ViolationRule {
                coverageEntity = entity
                minCoverageRatio = expectedMinCoverage
                entityCountThreshold = expectedThreshold
            }
        }

        // WHEN
        val actualVerifiers: Iterable<CoverageVerifier> = IntellijVerifierFactory.buildVerifiers(ProjectData(), config)

        // THEN
        assertSoftly(actualVerifiers) {
            shouldHaveSize(1)
            first().rule shouldBe CoverageRuleWithThreshold(
                id = 0,
                coverageEntity = entity,
                valueType = ValueType.COVERED_RATE,
                min = expectedMinCoverage.toBigDecimal(),
                threshold = expectedThreshold,
            )
        }
    }
}
