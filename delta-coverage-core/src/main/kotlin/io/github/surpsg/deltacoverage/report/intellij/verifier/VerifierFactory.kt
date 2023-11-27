package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.verify.Verifier
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import java.math.BigDecimal

internal object VerifierFactory {

    fun buildVerifiers(
        projectData: ProjectData,
        violationRuleConfig: CoverageRulesConfig,
    ): Iterable<CoverageVerifier> {
        return violationRuleConfig.entitiesRules
            .asSequence()
            .filter { (_, rule) -> rule.minCoverageRatio > 0.0 }
            .mapIndexed { index, (entity, rule) ->
                entity.buildVerifier(index, rule)
            }
            .map { verifierBound -> CoverageVerifier(projectData, verifierBound) }
            .toList()
    }

    private fun CoverageEntity.buildVerifier(
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
