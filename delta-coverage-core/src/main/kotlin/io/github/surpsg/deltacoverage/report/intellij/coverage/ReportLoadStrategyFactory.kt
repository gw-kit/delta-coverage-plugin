package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.api.Filters
import com.intellij.rt.coverage.report.data.BinaryReport
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.ReportBound

internal object ReportLoadStrategyFactory {

    fun buildReportLoadStrategies(reportContext: ReportContext): Sequence<NamedReportLoadStrategy> {
        val binaryReports: List<BinaryReport> = buildBinaryReports(reportContext)
        val intellijSourceInputs = IntellijSourceInputs(
            classesFiles = reportContext.deltaCoverageConfig.classFiles.toList(),
            sourcesFiles = reportContext.deltaCoverageConfig.sourceFiles.toList()
        )

        val filterProjectData: ProjectData = IntellijDeltaCoverageLoader.getDeltaProjectData(
            binaryReports,
            intellijSourceInputs,
            reportContext.codeUpdateInfo
        )

        val deltaReportLoadStrategy = sequenceOf(
            NamedReportLoadStrategy(
                ReportBound.DELTA_REPORT,
                PreloadedCoverageReportLoadStrategy(filterProjectData, binaryReports, intellijSourceInputs),
            )
        )
        return if (reportContext.deltaCoverageConfig.reportsConfig.fullCoverageReport) {
            deltaReportLoadStrategy + NamedReportLoadStrategy(
                ReportBound.FULL_REPORT,
                buildRawReportLoadStrategy(binaryReports, intellijSourceInputs)
            )
        } else {
            deltaReportLoadStrategy
        }
    }

    private fun buildBinaryReports(reportContext: ReportContext): List<BinaryReport> {
        return reportContext.deltaCoverageConfig.binaryCoverageFiles.map { binaryCoverageFile ->
            BinaryReport(binaryCoverageFile, null)
        }
    }

    private class PreloadedCoverageReportLoadStrategy(
        private val coverageData: ProjectData,
        binaryReports: List<BinaryReport>,
        intellijSourceInputs: IntellijSourceInputs
    ) : ReportLoadStrategy(
        binaryReports,
        intellijSourceInputs.classesFiles,
        intellijSourceInputs.sourcesFiles,
    ) {
        override fun loadProjectData(): ProjectData = coverageData
    }

    private fun buildRawReportLoadStrategy(
        binaryReports: List<BinaryReport>,
        intellijSourceInputs: IntellijSourceInputs
    ): ReportLoadStrategy = ReportLoadStrategy.RawReportLoadStrategy(
        binaryReports,
        intellijSourceInputs.classesFiles,
        intellijSourceInputs.sourcesFiles,
        Filters.EMPTY
    )
}
