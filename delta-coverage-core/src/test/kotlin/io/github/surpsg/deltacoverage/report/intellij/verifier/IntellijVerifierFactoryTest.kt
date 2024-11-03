package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.verify.api.ValueType
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class IntellijVerifierFactoryTest {

    @Test
    fun `buildVerifiers should return verifier if coverage ratio is set`() {
        // GIVEN
        val entity = CoverageEntity.LINE
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
            shouldHaveSize(3)
            map { it.rule }.shouldContainExactly(
                CoverageRuleWithThreshold(
                    id = 0,
                    coverageEntity = CoverageEntity.INSTRUCTION,
                    valueType = ValueType.COVERED_RATE,
                    min = BigDecimal.valueOf(0.0),
                    threshold = null,
                ),
                CoverageRuleWithThreshold(
                    id = 1,
                    coverageEntity = CoverageEntity.BRANCH,
                    valueType = ValueType.COVERED_RATE,
                    min = BigDecimal.valueOf(0.0),
                    threshold = null,
                ),
                CoverageRuleWithThreshold(
                    id = 2,
                    coverageEntity = entity,
                    valueType = ValueType.COVERED_RATE,
                    min = expectedMinCoverage.toBigDecimal(),
                    threshold = expectedThreshold,
                ),
            )
        }
    }
}
