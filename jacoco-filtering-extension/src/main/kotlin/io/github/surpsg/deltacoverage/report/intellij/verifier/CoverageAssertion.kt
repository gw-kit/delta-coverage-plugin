package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.exception.CoverageViolatedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal object CoverageAssertion {

    private val log: Logger = LoggerFactory.getLogger(CoverageAssertion::class.java)

    fun verify(
        projectData: ProjectData,
        coverageRulesConfig: CoverageRulesConfig
    ) {
        val violations: List<CoverageVerifier.Violation> = VerifierFactory
            .buildVerifiers(projectData, coverageRulesConfig)
            .flatMap { coverageVerifier -> coverageVerifier.verify() }

        if (coverageRulesConfig.failOnViolation) {
            throwIfCoverageViolated(violations)
        } else {
            printViolations(violations)
        }
    }

    private fun throwIfCoverageViolated(violations: List<CoverageVerifier.Violation>) {
        if (violations.isNotEmpty()) {
            val errorDetails: String = violations.joinToString(";\n") {
                it.buildCoverageViolatedMessage()
            }
            throw CoverageViolatedException(errorDetails)
        }
    }

    private fun printViolations(violations: List<CoverageVerifier.Violation>) {
        violations.forEach { violation ->
            log.warn(violation.buildCoverageViolatedMessage())
        }
    }

    private fun CoverageVerifier.Violation.buildCoverageViolatedMessage(): String {
        return "$coverageTrackType: expectedMin=$expectedMinValue, actual=$actualValue"
    }

}
