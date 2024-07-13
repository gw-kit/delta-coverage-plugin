package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.CoverageRulesConfig

internal data class CoverageVerificationResult(
    val view: String,
    val coverageRulesConfig: CoverageRulesConfig,
    val violations: List<String>,
) {

    fun contextualViolations(): List<String> =
        violations.map { violation -> "[view:$view] $violation" }
}
