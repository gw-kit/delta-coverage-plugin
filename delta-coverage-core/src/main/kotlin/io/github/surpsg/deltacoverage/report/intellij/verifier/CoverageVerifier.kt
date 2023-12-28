package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.verify.Verifier

internal class CoverageVerifier(
    private val projectData: ProjectData,
    internal val rule: CoverageRuleWithThreshold,
) {

    fun verify(): Iterable<Violation> {
        val violationsCollector = CoverageViolationsCollector(rule)
        Verifier.Target.ALL.createTargetProcessor().process(projectData, violationsCollector)
        return violationsCollector.violations
    }

    data class Violation(
        val coverageTrackType: String,
        val expectedMinValue: Double,
        val actualValue: Double,
    )
}
