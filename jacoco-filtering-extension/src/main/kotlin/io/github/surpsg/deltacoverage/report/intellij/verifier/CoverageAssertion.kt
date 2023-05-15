package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.report.CoverageViolationsPropagator

internal object CoverageAssertion {

    private val coverageViolationsPropagator = CoverageViolationsPropagator()

    fun verify(
        projectData: ProjectData,
        coverageRulesConfig: CoverageRulesConfig
    ) {
        val violations: List<String> = VerifierFactory
            .buildVerifiers(projectData, coverageRulesConfig)
            .flatMap { coverageVerifier -> coverageVerifier.verify() }
            .map { it.buildCoverageViolatedMessage() }

        coverageViolationsPropagator.propagate(coverageRulesConfig, violations)
    }

    private fun CoverageVerifier.Violation.buildCoverageViolatedMessage(): String {
        return "$coverageTrackType: expectedMin=$expectedMinValue, actual=$actualValue"
    }
}
