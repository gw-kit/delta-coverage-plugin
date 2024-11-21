package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.data.instructions.ClassInstructions
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.ReportBound
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CoverageAssertionTest {

    @Test
    fun `should return verification result with empty violations when no coverage rules`() {
        // GIVEN
        val view = "not-violated"
        val coverageRulesConfig = CoverageRulesConfig {}

        // WHEN
        val coverageSummary: CoverageSummary = CoverageAssertion.verify(
            view,
            ProjectData(),
            coverageRulesConfig
        )

        // THEN
        coverageSummary shouldBe CoverageSummary(
            view = view,
            reportBound = ReportBound.DELTA_REPORT,
            coverageRulesConfig = coverageRulesConfig,
            verifications = emptyList(),
            coverageInfo = listOf(
                CoverageSummary.Info(CoverageEntity.INSTRUCTION, 0, 0),
                CoverageSummary.Info(CoverageEntity.BRANCH, 0, 0),
                CoverageSummary.Info(CoverageEntity.LINE, 0, 0),
            ),
        )
    }

    @Test
    fun `should return verification result with violations when violation found`() {
        // GIVEN
        val view = "violated"
        val entity = CoverageEntity.LINE
        val coverageRulesConfig = CoverageRulesConfig {
            violationRules += ViolationRule {
                coverageEntity = entity
                minCoverageRatio = 1.0
            }
        }

        // WHEN
        val coverageSummary: CoverageSummary = CoverageAssertion.verify(
            view,
            buildCoverageProjectData(),
            coverageRulesConfig,
        )

        // THEN
        coverageSummary shouldBe CoverageSummary(
            view = view,
            reportBound = ReportBound.DELTA_REPORT,
            coverageRulesConfig = coverageRulesConfig,
            coverageInfo = listOf(
                CoverageSummary.Info(CoverageEntity.INSTRUCTION, 0, 0),
                CoverageSummary.Info(CoverageEntity.BRANCH, 0, 0),
                CoverageSummary.Info(CoverageEntity.LINE, 0, 1),
            ),
            verifications = listOf(
                CoverageSummary.VerificationResult(
                    coverageEntity = entity,
                    violation = ("$entity: expectedMin=1.0, actual=0.0"),
                )
            )
        )
    }

    private fun buildCoverageProjectData(): ProjectData {
        val className = "com.example.SomeClass"
        val sourceName = "SomeClass.java"
        return ProjectData().apply {
            instructions[className] = ClassInstructions(emptyArray())

            getOrCreateClassData(className).apply {
                source = sourceName
                setLines(arrayOf(LineData(1, "")))
            }
        }
    }
}
