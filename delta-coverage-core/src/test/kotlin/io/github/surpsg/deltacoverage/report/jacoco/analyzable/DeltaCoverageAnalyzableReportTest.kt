package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.jacoco.report.VerifiableReportVisitor
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test

class DeltaCoverageAnalyzableReportTest {

    @Test
    fun `should build visitor`() {
        // GIVEN
        val deltaCoverageReport = DeltaCoverageAnalyzableReport(
            jacocoReports = emptyList(),
            reportContext = ReportContext(
                DeltaCoverageConfig {
                    diffSource = mockk()
                }
            ),
        )

        // WHEN
        val visitor: VerifiableReportVisitor = deltaCoverageReport.buildVisitor()

        // THEN
        assertSoftly(visitor) {
            reportBound shouldBe ReportBound.DELTA_REPORT
        }
    }
}
