package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.data.instructions.ClassInstructions
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.report.CoverageVerificationResult
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CoverageAssertionTest {

    @Test
    fun `should return verification result with empty violations when no coverage rules`() {
        // GIVEN
        val view = "not-violated"
        val coverageRulesConfig = CoverageRulesConfig {}

        // WHEN
        val verificationResults: CoverageVerificationResult = CoverageAssertion.verify(
            view,
            ProjectData(),
            coverageRulesConfig
        )

        // THEN
        verificationResults shouldBe CoverageVerificationResult(
            view = view,
            coverageRulesConfig = coverageRulesConfig,
            violations = emptyList()
        )
    }

    @Test
    fun `should return verification result with violations when violation found`() {
        // GIVEN
        val view = "violated"
        val coverageRulesConfig = CoverageRulesConfig {
            violationRules += ViolationRule {
                coverageEntity = CoverageEntity.LINE
                minCoverageRatio = 1.0
            }
        }

        // WHEN
        val verificationResults: CoverageVerificationResult = CoverageAssertion.verify(
            view,
            buildCoverageProjectData(),
            coverageRulesConfig
        )

        // THEN
        verificationResults shouldBe CoverageVerificationResult(
            view = view,
            coverageRulesConfig = coverageRulesConfig,
            violations = listOf("LINE: expectedMin=1.0, actual=0.0")
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
