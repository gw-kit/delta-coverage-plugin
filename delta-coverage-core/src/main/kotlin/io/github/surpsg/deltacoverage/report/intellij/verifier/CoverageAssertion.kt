package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.report.CoverageViolationsPropagator

internal object CoverageAssertion {

    private val coverageViolationsPropagator = CoverageViolationsPropagator()

    fun verify(
        view: String,
        projectData: ProjectData,
        coverageRulesConfig: CoverageRulesConfig
    ) {
        val violations: List<String> = IntellijVerifierFactory
            .buildVerifiers(projectData, coverageRulesConfig)
            .flatMap { coverageVerifier -> coverageVerifier.verify() }
            .map { it.buildCoverageViolatedMessage(view) }

        coverageViolationsPropagator.propagate(view, coverageRulesConfig, violations)
    }

    private fun CoverageVerifier.Violation.buildCoverageViolatedMessage(view: String): String {
        return "[view:$view] $coverageTrackType: expectedMin=$expectedMinValue, actual=$actualValue"
    }
}
