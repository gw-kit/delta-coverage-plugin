package io.github.surpsg.deltacoverage.report.jacoco.verification

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.report.violation.ViolationResolveContext
import io.github.surpsg.deltacoverage.report.violation.ViolationResolveContext.Companion.NO_IGNORE_VIOLATION_CONTEXT
import org.jacoco.core.analysis.ICoverageNode
import org.jacoco.report.check.IViolationsOutput
import org.jacoco.report.check.Limit
import org.jacoco.report.check.Rule
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// TODO duplicates intellij coverage io.github.surpsg.deltacoverage.report.intellij.verifier.CoverageViolationsCollector
internal class ViolationsOutputResolver(
    violationRuleConfig: CoverageRulesConfig
) : IViolationsOutput {

    private val violationRules: Map<CoverageEntity, ViolationRule> by lazy { violationRuleConfig.entitiesRules }

    private val foundViolations: MutableMap<CoverageEntity, String> = mutableMapOf()

    fun getViolations(): Map<CoverageEntity, String> = foundViolations

    override fun onViolation(node: ICoverageNode, rule: Rule, limit: Limit, message: String) {
        log.debug("New violation: $message")

        val violationResolveContext: ViolationResolveContext = buildViolationResolveContext(limit.entity, node)
        if (violationResolveContext.isIgnoredByThreshold()) {
            log.info(
                "Coverage violation of {} was ignored because threshold={} but total={}",
                violationResolveContext.coverageEntity,
                violationResolveContext.thresholdCount,
                violationResolveContext.totalCount,
            )
        } else {
            limit.entity.mapToCoverageEntity()?.let { coverageEntity ->
                foundViolations[coverageEntity] = message
            }
        }
    }

    private fun buildViolationResolveContext(
        counterEntity: ICoverageNode.CounterEntity,
        coverage: ICoverageNode,
    ): ViolationResolveContext {
        val totalEntityCount: Int = coverage.getCounter(counterEntity).totalCount
        val coverageEntity: CoverageEntity = counterEntity.mapToCoverageEntity() ?: return NO_IGNORE_VIOLATION_CONTEXT

        return violationRules[coverageEntity]
            ?.entityCountThreshold
            ?.let { threshold -> ViolationResolveContext(coverageEntity, threshold, totalEntityCount.toLong()) }
            ?: NO_IGNORE_VIOLATION_CONTEXT
    }

    private fun ICoverageNode.CounterEntity.mapToCoverageEntity(): CoverageEntity? = when (this) {
        ICoverageNode.CounterEntity.INSTRUCTION -> CoverageEntity.INSTRUCTION
        ICoverageNode.CounterEntity.BRANCH -> CoverageEntity.BRANCH
        ICoverageNode.CounterEntity.LINE -> CoverageEntity.LINE
        else -> null
    }

    private companion object {
        val log: Logger = LoggerFactory.getLogger(ViolationsOutputResolver::class.java)
    }
}
