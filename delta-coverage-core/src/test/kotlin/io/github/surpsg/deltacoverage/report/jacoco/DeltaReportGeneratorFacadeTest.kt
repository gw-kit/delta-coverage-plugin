package io.github.surpsg.deltacoverage.report.jacoco

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.DeltaReportGeneratorFacade
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.kotest.matchers.string.shouldContain
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.FileSystem
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText

@ExtendWith(MockKExtension::class)
class DeltaReportGeneratorFacadeTest {

    private val testFileSystem: FileSystem = Jimfs.newFileSystem(Configuration.unix())

    @Suppress("VarCouldBeVal")
    @SpyK
    private var deltaReportGeneratorFacade = object : DeltaReportGeneratorFacade() {
        override fun generate(reportContext: ReportContext) = mapOf(
            ReportBound.DELTA_REPORT to CoverageSummary(
                view = "",
                reportBound = ReportBound.DELTA_REPORT,
                coverageRulesConfig = CoverageRulesConfig {},
                verifications = emptyList(),
                coverageInfo = setOf(),
            ),
            ReportBound.FULL_REPORT to CoverageSummary(
                view = "",
                reportBound = ReportBound.FULL_REPORT,
                coverageRulesConfig = CoverageRulesConfig {},
                verifications = emptyList(),
                coverageInfo = setOf(),
            ),
        )
    }

    @ParameterizedTest
    @EnumSource(CoverageEngine::class)
    fun `should generate reports by all configs`(engine: CoverageEngine) {
        // GIVEN
        val baseDir = testFileSystem.getPath("/base").createDirectories()
        val view = "test-view"
        val config = DeltaCoverageConfig {
            fileSystem = testFileSystem
            coverageEngine = engine

            viewName = view
            diffSource = mockk()
            reportsConfig = ReportsConfig {
                baseReportDir = baseDir.toString()
                fullCoverageReport = true
            }
        }

        val summaryFile: Path = baseDir.resolve("$view-summary.json")
        val fullCoverageSummaryFile: Path = baseDir.resolve("full-coverage-$view-summary.json")

        // WHEN
        deltaReportGeneratorFacade.generateReports(config)

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

        // AND THEN
        fullCoverageSummaryFile.readText().shouldContain(
            """
                "reportBound":"FULL_REPORT"
                """.trimIndent()
        )
    }
}
