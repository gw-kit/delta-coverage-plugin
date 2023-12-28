package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.verify.Verifier
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import java.math.BigDecimal

internal object IntellijVerifierFactory {

    fun buildVerifiers(
        projectData: ProjectData,
        violationRuleConfig: CoverageRulesConfig,
    ): Iterable<CoverageVerifier> {
        return violationRuleConfig.entitiesRules.asSequence()
            .mapIndexed { index, (entity, rule) ->
                entity.buildRule(index, rule)
            }
            .map { rule -> CoverageVerifier(projectData, rule) }
            .toList()
    }

    private fun CoverageEntity.buildRule(
        id: Int,
        violationRule: ViolationRule
    ) = CoverageRuleWithThreshold( // TODO maybe extract common part from jacoco and this one
        id,
        this,
        Verifier.ValueType.COVERED_RATE,
        BigDecimal.valueOf(violationRule.minCoverageRatio),
        violationRule.entityCountThreshold,
    )
}
