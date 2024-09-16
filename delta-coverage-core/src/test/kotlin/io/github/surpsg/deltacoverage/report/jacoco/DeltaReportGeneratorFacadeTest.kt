package io.github.surpsg.deltacoverage.report.jacoco

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.report.CoverageVerificationResult
import io.github.surpsg.deltacoverage.report.DeltaReportGeneratorFacade
import io.github.surpsg.deltacoverage.report.ReportContext
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DeltaReportGeneratorFacadeTest {

    @Suppress("VarCouldBeVal")
    @SpyK
    private var deltaReportGeneratorFacade = object : DeltaReportGeneratorFacade() {
        override fun generate(reportContext: ReportContext) = emptyList<CoverageVerificationResult>()
    }

    @Test
    fun `should generate reports by all configs`() {
        // GIVEN
        val configsNumber = 3
        val configs = List(configsNumber) { index ->
            DeltaCoverageConfig {
                viewName = "view$index"
                diffSource = mockk()
            }
        }

        // WHEN
        deltaReportGeneratorFacade.generateReports(configs)

        // THEN
        verify(exactly = configsNumber) { deltaReportGeneratorFacade.generate(any()) }
    }

}
