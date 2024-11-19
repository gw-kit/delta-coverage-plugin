package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.exception.CoverageViolatedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

internal class CoverageViolationsPropagator {

    fun propagateAll(
        coverageSummary: CoverageSummary,
    ) {
        val exceptionMsg: String = sequenceOf(coverageSummary)
            .filter { result -> filterOutSoftViolations(result) }
            .flatMap { result -> result.contextualViolations().asSequence() }
            .joinToString(";\n")

        if (exceptionMsg.isNotBlank()) {
            throw CoverageViolatedException(exceptionMsg)
        }
    }

    private fun filterOutSoftViolations(
        coverageSummary: CoverageSummary,
    ): Boolean {
        log.info(
            "[{}] Fail on violations: {}. Found violations: {}.",
            coverageSummary.view,
            coverageSummary.coverageRulesConfig.failOnViolation,
            coverageSummary.verifications.size,
        )
        return if (coverageSummary.coverageRulesConfig.failOnViolation) {
            true
        } else {
            coverageSummary.verifications.forEach { verification ->
                log.warn("[{}] {}", coverageSummary.view, verification.violation)
            }
            false
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    }
}
