package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.verify.api.Target
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.report.CoverageSummary

internal class CoverageVerifier(
    private val projectData: ProjectData,
    internal val rule: CoverageRuleWithThreshold,
) {

    fun verify(): VerifiedCoverage {
        val violationsCollector = CoverageViolationsCollector(rule)
        Target.ALL.createTargetProcessor().process(projectData, violationsCollector)
        return VerifiedCoverage(
            violations = violationsCollector.violations,
            info = violationsCollector.coverageInfo,
        )
    }

    data class VerifiedCoverage(
        val violations: List<Violation>,
        val info: List<CoverageSummary.Info>,
    )

    data class Violation(
        val coverageEntity: CoverageEntity,
        val expectedMinValue: Double,
        val actualValue: Double,
    )
}
