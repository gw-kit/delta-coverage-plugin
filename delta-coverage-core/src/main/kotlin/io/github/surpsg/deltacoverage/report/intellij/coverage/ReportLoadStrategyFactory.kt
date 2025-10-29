package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.data.BinaryReport
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.intellij.coverage.loader.FullCoverageDataLoader
import io.github.surpsg.deltacoverage.report.intellij.coverage.loader.IntellijDeltaCoverageLoader

internal object ReportLoadStrategyFactory {

    fun buildReportLoadStrategies(reportContext: ReportContext): Sequence<NamedReportLoadStrategy> {
        val binaryReports: List<BinaryReport> = buildBinaryReports(reportContext)
        val intellijSourceInputs = IntellijSourceInputs(
            allClasses = reportContext.deltaCoverageConfig.classFiles,
            classesRoots = reportContext.deltaCoverageConfig.classRoots.toList(),
            excludeClasses = reportContext.excludeClasses,
            sourcesFiles = reportContext.srcFiles.toList(),
        )

        val fullCoverageData: ProjectData = FullCoverageDataLoader()
            .load(binaryReports, intellijSourceInputs)

        val filterProjectData: ProjectData = IntellijDeltaCoverageLoader.getDeltaProjectData(
            fullCoverageData,
            reportContext.codeUpdateInfo,
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
                PreloadedCoverageReportLoadStrategy(fullCoverageData, binaryReports, intellijSourceInputs),
            )
        } else {
            deltaReportLoadStrategy
        }
    }

    private fun buildBinaryReports(reportContext: ReportContext): List<BinaryReport> {
        return reportContext.deltaCoverageConfig.binaryCoverageFiles.filter { it.exists() }.map { binaryCoverageFile ->
            BinaryReport(binaryCoverageFile, null)
        }
    }

    private class PreloadedCoverageReportLoadStrategy(
        private val coverageData: ProjectData,
        binaryReports: List<BinaryReport>,
        intellijSourceInputs: IntellijSourceInputs
    ) : ReportLoadStrategy(
        binaryReports,
        intellijSourceInputs.classesRoots,
        intellijSourceInputs.sourcesFiles,
    ) {
        override fun loadProjectData(): ProjectData = coverageData
    }
}

