package io.github.surpsg.deltacoverage.report.jacoco

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equality.shouldBeEqualUsingFields
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.maps.shouldContainKeys
import io.kotest.matchers.maps.shouldContainValue
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class JacocoDeltaReportGeneratorFacadeTest {

    @Test
    fun `should return empty summary when no classes`() {
        // GIVEN
        val reportGeneratorFacade = JacocoDeltaReportGeneratorFacade()
        val testViewName = "testView"
        val context = ReportContext(
            DeltaCoverageConfig {
                viewName = testViewName
                diffSource = mockk<DiffSource> {
                    every { pullDiff() } returns emptyList()
                }

                reportsConfig = ReportsConfig {
                    val reportConfig = ReportConfig { enabled = false }
                    html = reportConfig
                    xml = reportConfig
                    console = reportConfig
                    markdown = reportConfig
                    fullCoverageReport = true
                }
            }
        )

        // WHEN
        val actual: Map<ReportBound, CoverageSummary> = reportGeneratorFacade.generate(context)

        // THEN
        actual.shouldContainKeys(*ReportBound.entries.toTypedArray())
        val expectedDeltaSummary = CoverageSummary(
            view = testViewName,
            reportBound = ReportBound.DELTA_REPORT,
            coverageRulesConfig = CoverageRulesConfig {},
            verifications = emptyList(),
            coverageInfo = emptySet(),
        )
        actual.getValue(ReportBound.DELTA_REPORT) shouldMatchTo expectedDeltaSummary
        actual.getValue(ReportBound.FULL_REPORT) shouldMatchTo expectedDeltaSummary.copy(
            reportBound = ReportBound.FULL_REPORT
        )
    }

    private infix fun CoverageSummary.shouldMatchTo(expected: CoverageSummary) {
        val actualSummary = this
        actualSummary.shouldBeEqualUsingFields {
            excludedProperties = setOf(
                CoverageSummary::coverageRulesConfig,
                CoverageSummary::coverageInfo,
            )
            expected
        }
        actualSummary.coverageInfo shouldContainExactlyInAnyOrder CoverageEntity.entries.map { entity ->
            CoverageSummary.Info(entity, 0, 0)
        }
    }
}

