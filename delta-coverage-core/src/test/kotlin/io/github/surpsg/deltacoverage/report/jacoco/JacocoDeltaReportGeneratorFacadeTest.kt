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
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equality.shouldBeEqualUsingFields
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
                }
            }
        )

        // WHEN
        val actual: CoverageSummary = reportGeneratorFacade.generate(context)

        // THEN
        actual.shouldBeEqualUsingFields {
            excludedProperties = setOf(
                CoverageSummary::coverageRulesConfig,
                CoverageSummary::coverageInfo,
            )
            CoverageSummary(
                view = testViewName,
                reportBound = ReportBound.DELTA_REPORT,
                coverageRulesConfig = CoverageRulesConfig {},
                verifications = emptyList(),
                coverageInfo = emptyList(),
            )
        }

        // AND THEN
        actual.coverageInfo shouldContainExactlyInAnyOrder CoverageEntity.entries
            .map { entity -> CoverageSummary.Info(entity, 0, 0) }
    }
}

