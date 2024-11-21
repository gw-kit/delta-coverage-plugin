package io.github.surpsg.deltacoverage.report.jacoco

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.DeltaReportGeneratorFacade
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.kotest.matchers.string.shouldContain
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.FileSystem
import java.nio.file.Path
import kotlin.io.path.readText

@ExtendWith(MockKExtension::class)
class DeltaReportGeneratorFacadeTest {

    private val fileSystem: FileSystem = Jimfs.newFileSystem(Configuration.unix())

    @Suppress("VarCouldBeVal")
    @SpyK
    private var deltaReportGeneratorFacade = object : DeltaReportGeneratorFacade() {
        override fun generate(reportContext: ReportContext) = CoverageSummary(
            view = "",
            reportBound = ReportBound.DELTA_REPORT,
            coverageRulesConfig = CoverageRulesConfig {},
            verifications = emptyList(),
            coverageInfo = emptyList(),
        )
    }

    @Test
    fun `should generate reports by all configs`() {
        // GIVEN
        val config = DeltaCoverageConfig {
            viewName = "view"
            diffSource = mockk()
        }
        val summaryFile: Path = fileSystem.getPath("/summary-1.json")

        // WHEN
        deltaReportGeneratorFacade.generateReports(summaryFile, config)

        // THEN
        verify { deltaReportGeneratorFacade.generate(any()) }
        summaryFile.readText()
            .shouldContain(
                """
                "view":""
                """.trimIndent()
            )
            .shouldContain(
                """
                "reportBound":"DELTA_REPORT"
                """.trimIndent()
            )
    }
}
