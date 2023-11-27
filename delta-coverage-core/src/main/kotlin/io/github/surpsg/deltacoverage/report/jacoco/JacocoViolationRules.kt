package io.github.surpsg.deltacoverage.report.jacoco

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import org.jacoco.core.analysis.ICoverageNode
import org.jacoco.report.check.Limit
import org.jacoco.report.check.Rule

internal fun CoverageRulesConfig.buildRules(): List<Rule> {
    val limits: List<Limit> = entitiesRules.asSequence()
        .map { (coverageEntity, minValue) ->
            coverageEntity.toJacocoEntity() to minValue.minCoverageRatio
        }
        .filter { (_, minCoverage) ->
            minCoverage > 0.0
        }.map { (counterType, minCoverage) ->
            Limit().apply {
                setCounter(counterType.name)
                minimum = minCoverage.toString()
            }
        }.toList()

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
