package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.report.CoverageVerificationResult

internal object CoverageAssertion {

    fun verify(
        view: String,
        projectData: ProjectData,
        coverageRulesConfig: CoverageRulesConfig
    ): CoverageVerificationResult {
        val violations: List<String> = IntellijVerifierFactory
            .buildVerifiers(projectData, coverageRulesConfig)
            .flatMap { coverageVerifier -> coverageVerifier.verify() }
            .map { it.buildCoverageViolatedMessage(view) }

        return CoverageVerificationResult(
            view = view,
            coverageRulesConfig = coverageRulesConfig,
            violations = violations,
        )
    }

    private fun CoverageVerifier.Violation.buildCoverageViolatedMessage(view: String): String {
        return "[$view] $coverageTrackType: expectedMin=$expectedMinValue, actual=$actualValue"
    }
}
