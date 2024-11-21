package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.verify.api.ValueType
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import java.math.BigDecimal

internal object IntellijVerifierFactory {

    fun buildVerifiers(
        projectData: ProjectData,
        violationRuleConfig: CoverageRulesConfig,
    ): Iterable<CoverageVerifier> {
        return CoverageEntity.entries.asSequence()
            .map { entity ->
                violationRuleConfig.entitiesRules[entity] ?: entity.noOpViolationRule()
            }
            .mapIndexed { index, violationRule -> buildCoverageRule(index, violationRule) }
            .map { rule -> CoverageVerifier(projectData, rule) }
            .toList()
    }

    private fun buildCoverageRule(
        id: Int,
        violationRule: ViolationRule
    ) = CoverageRuleWithThreshold( // TODO maybe extract common part from jacoco and this one
        id,
        violationRule.coverageEntity,
        ValueType.COVERED_RATE,
        BigDecimal.valueOf(violationRule.minCoverageRatio),
        violationRule.entityCountThreshold,
    )

    private fun CoverageEntity.noOpViolationRule() = ViolationRule {
        coverageEntity = this@noOpViolationRule
        minCoverageRatio = 0.0
    }
}
