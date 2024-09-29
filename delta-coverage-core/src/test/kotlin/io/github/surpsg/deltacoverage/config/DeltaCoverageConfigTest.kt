package io.github.surpsg.deltacoverage.config

import io.github.surpsg.deltacoverage.diff.DiffSource
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

class DeltaCoverageConfigTest {

    @Test
    fun `should build delta coverage config with defaults`() {
        // GIVEN
        val expectedDiffSource = mockk<DiffSource>(relaxed = true)
        val expectedViewName = "delta-coverage-report"

        // WHEN
        val actualConfig = DeltaCoverageConfig {
            viewName = expectedViewName
            diffSource = expectedDiffSource
        }

        // THEN
        assertSoftly(actualConfig) {
            view shouldBeEqualComparingTo expectedViewName
            diffSource shouldBe expectedDiffSource
            reportsConfig shouldBeEqualToComparingFields ReportsConfig {}.apply { view = expectedViewName }
            coverageRulesConfig shouldBeEqualToComparingFields CoverageRulesConfig {}
            binaryCoverageFiles.shouldBeEmpty()
            classFiles.shouldBeEmpty()
            sourceFiles.shouldBeEmpty()
        }
    }

    @Test
    fun `should throw if diff source is not configured`() {
        shouldThrow<IllegalArgumentException> {
            DeltaCoverageConfig {}
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "\t\t\t", "\n\n"])
    fun `should throw if view name is blank`(blankViewName: String) {
        // GIVEN
        val expectedDiffSource = mockk<DiffSource>()

        // WHEN // THEN
        shouldThrow<IllegalArgumentException> {
            DeltaCoverageConfig {
                viewName = blankViewName
                diffSource = expectedDiffSource
            }
        }
    }

    @Test
    fun `should build delta coverage config with custom properties`() {
        // GIVEN
        val expectedReportName = "report-name"
        val expectedDiffSource = mockk<DiffSource>(relaxed = true)
        val expectedReportsConfig = ReportsConfig { baseReportDir = "some/custom" }
            .apply { view = expectedReportName }
        val expectedCoverageRulesConfig = mockk<CoverageRulesConfig>()
        val expectedBinaries = listOf(File("exec"))
        val expectedSources = listOf(File("sources"))
        val expectedClasses = listOf(File("classes"))

        // WHEN
        val actualConfig = DeltaCoverageConfig {
            viewName = expectedReportName
            diffSource = expectedDiffSource
            reportsConfig = expectedReportsConfig
            coverageRulesConfig = expectedCoverageRulesConfig
            binaryCoverageFiles += expectedBinaries
            classFiles += expectedClasses
            sourceFiles += expectedSources
        }

        // THEN
        assertSoftly(actualConfig) {
            diffSource shouldBe expectedDiffSource
            view shouldBeEqualComparingTo expectedReportName
            reportsConfig shouldBe expectedReportsConfig
            coverageRulesConfig shouldBe expectedCoverageRulesConfig
            binaryCoverageFiles shouldContainExactly expectedBinaries
            classFiles shouldContainExactly expectedClasses
            sourceFiles shouldContainExactly expectedSources
        }
    }
}
