package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportType
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ConsoleReportBuilderTest {

    @ParameterizedTest
    @EnumSource(value = ReportType::class, names = ["CONSOLE", "MARKDOWN"])
    fun `should not generate report if report bound is not delta coverage`(
        reportType: ReportType,
    ) {
        // GIVEN
        val reporter = mockk<Reporter>()
        val builder = ConsoleReportBuilder(
            reportBound = ReportBound.FULL_REPORT,
            reporter = reporter
        )

        // WHEN
        builder.buildReport()

        // THEN
        verify { reporter wasNot Called }
    }

    @ParameterizedTest
    @EnumSource(value = ReportType::class, names = ["CONSOLE", "MARKDOWN"])
    fun `should generate report if report bound is delta coverage`(
        reportType: ReportType,
    ) {
        // GIVEN
        val reporter = mockk<Reporter> {
            every { projectData } returns ProjectData()
        }
        val builder = ConsoleReportBuilder(
            reportBound = ReportBound.DELTA_REPORT,
            reporter = reporter
        )

        // WHEN
        builder.buildReport()

        // THEN
        verify(exactly = 1) {
            reporter.projectData
        }
    }
}
