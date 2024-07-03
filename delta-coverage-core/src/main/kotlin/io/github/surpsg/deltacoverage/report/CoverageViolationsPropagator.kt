package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.exception.CoverageViolatedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class CoverageViolationsPropagator {

    fun propagate(
        view: String = "default",
        coverageRulesConfig: CoverageRulesConfig,
        violations: List<String>,
    ) {
        log.info(
            "[view:{}] Fail on violations: {}. Found violations: {}.",
            view,
            coverageRulesConfig.failOnViolation,
            violations.size
        )
        if (coverageRulesConfig.failOnViolation) {
            throwIfCoverageViolated(violations)
        } else {
            violations.forEach { violation ->
                log.warn(violation)
            }
        }
    }

    private fun throwIfCoverageViolated(violations: List<String>) {
        if (violations.isNotEmpty()) {
            val errorDetails: String = violations.joinToString(";\n")
            throw CoverageViolatedException(errorDetails)
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CoverageViolationsPropagator::class.java)
    }
}
