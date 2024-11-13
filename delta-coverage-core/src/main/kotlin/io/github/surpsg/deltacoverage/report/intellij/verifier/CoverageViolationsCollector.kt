package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.verify.TargetProcessor
import com.intellij.rt.coverage.verify.Verifier
import com.intellij.rt.coverage.verify.api.Counter
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.report.CoverageSummary.Info
import io.github.surpsg.deltacoverage.report.violation.ViolationResolveContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal

internal class CoverageViolationsCollector(
    private val rule: CoverageRuleWithThreshold,
) : TargetProcessor.Consumer {

    private val foundViolations: MutableList<CoverageVerifier.Violation> = mutableListOf()
    private val collectedCoverageInfo: MutableList<Info> = mutableListOf()

    val violations: List<CoverageVerifier.Violation> = foundViolations

    val coverageInfo: List<Info> = collectedCoverageInfo

    override fun consume(name: String, coverage: Verifier.CollectedCoverage) {
        val counter: Verifier.CollectedCoverage.Counter = rule.coverageEntity.toVerifierCounter()
            .getCounter(coverage)
            .apply { collectCoverageInfo(this) }

        val violationResolveContext: ViolationResolveContext = buildViolationResolveContext(
            counter,
            rule.coverageEntity
        )
        if (violationResolveContext.isIgnoredByThreshold()) {
            log.info(
                "Coverage violation of {} was ignored because threshold={} but total={}",
                violationResolveContext.coverageEntity,
                violationResolveContext.thresholdCount,
                violationResolveContext.totalCount
            )
        } else {
            addViolation(counter)
        }
    }

    private fun collectCoverageInfo(counter: Verifier.CollectedCoverage.Counter) {
        collectedCoverageInfo += Info(
            coverageEntity = rule.coverageEntity,
            covered = counter.covered.toInt(),
            total = counter.calculateTotal().toInt()
        )
    }

    private fun addViolation(counter: Verifier.CollectedCoverage.Counter) {
        val actualValue: BigDecimal = rule.valueType.getValue(counter) ?: return
        if (actualValue < rule.min) {
            foundViolations += CoverageVerifier.Violation(
                coverageEntity = rule.coverageEntity,
                expectedMinValue = rule.min.toDouble(),
                actualValue = actualValue.toDouble()
            )
        }
    }

    private fun buildViolationResolveContext(
        collectedCoverageCounter: Verifier.CollectedCoverage.Counter,
        coverageEntity: CoverageEntity,
    ): ViolationResolveContext {
        return rule.threshold
            ?.let { threshold ->
                ViolationResolveContext(
                    coverageEntity,
                    threshold,
                    collectedCoverageCounter.calculateTotal()
                )
            }
            ?: ViolationResolveContext.NO_IGNORE_VIOLATION_CONTEXT
    }

    private fun Verifier.CollectedCoverage.Counter.calculateTotal(): Long = missed + covered

    private fun CoverageEntity.toVerifierCounter(): Counter = when (this) {
        CoverageEntity.INSTRUCTION -> Counter.INSTRUCTION
        CoverageEntity.BRANCH -> Counter.BRANCH
        CoverageEntity.LINE -> Counter.LINE
    }

    private companion object {
        val log: Logger = LoggerFactory.getLogger(CoverageViolationsCollector::class.java)
    }
}
