package io.github.surpsg.deltacoverage.report.jacoco.verification

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import org.jacoco.core.analysis.ICoverageNode
import org.jacoco.report.check.Limit
import org.jacoco.report.check.Rule

internal object JacocoVerifierFactory {

    internal fun buildRules(
        violationRuleConfig: CoverageRulesConfig,
    ): List<Rule> {
        val limits: List<Limit> = violationRuleConfig.entitiesRules.asSequence()
            .map { (coverageEntity, violationRule) ->
                val jacocoEntity: ICoverageNode.CounterEntity = coverageEntity.toJacocoEntity()
                Limit().apply {
                    setCounter(jacocoEntity.name)
                    minimum = violationRule.minCoverageRatio.toString()
                }
            }
            .toList()

        return if (limits.isNotEmpty()) {
            listOf(
                Rule().apply { this.limits = limits }
            )
        } else {
            emptyList()
        }
    }

    private fun CoverageEntity.toJacocoEntity(): ICoverageNode.CounterEntity {
        return when (this) {
            CoverageEntity.INSTRUCTION -> ICoverageNode.CounterEntity.INSTRUCTION
            CoverageEntity.BRANCH -> ICoverageNode.CounterEntity.BRANCH
            CoverageEntity.LINE -> ICoverageNode.CounterEntity.LINE
        }
    }
}
