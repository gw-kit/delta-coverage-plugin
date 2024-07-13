package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.exception.CoverageViolatedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

internal class CoverageViolationsPropagator {

    fun propagateAll(
        verificationResults: Iterable<CoverageVerificationResult>,
    ) {
        val exceptionMsg: String = verificationResults.asSequence()
            .mapNotNull { result -> filterOutSoftViolations(result) }
            .flatMap { result -> result.contextualViolations() }
            .joinToString(";\n")

        if (exceptionMsg.isNotBlank()) {
            throw CoverageViolatedException(exceptionMsg)
        }
    }

    private fun filterOutSoftViolations(
        verificationResult: CoverageVerificationResult,
    ): CoverageVerificationResult? {
        log.info(
            "[view:{}] Fail on violations: {}. Found violations: {}.",
            verificationResult.view,
            verificationResult.coverageRulesConfig.failOnViolation,
            verificationResult.violations.size,
        )
        return if (verificationResult.coverageRulesConfig.failOnViolation) {
            verificationResult
        } else {
            verificationResult.violations.forEach { violation ->
                log.warn(violation)
            }
            null
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    }
}
