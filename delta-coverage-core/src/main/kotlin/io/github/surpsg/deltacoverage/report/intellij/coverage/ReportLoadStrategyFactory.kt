package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.data.BinaryReport
import com.intellij.rt.coverage.report.data.Filters
import com.intellij.rt.coverage.report.data.Module
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.intellij.report.ReportBound

internal object ReportLoadStrategyFactory {

    private const val FULL_COVERAGE_REPORT = "full-coverage-report"

    fun buildReportLoadStrategies(reportContext: ReportContext): Sequence<NamedReportLoadStrategy> {
        val binaryReports: List<BinaryReport> = buildBinaryReports(reportContext)
        val modules: List<Module> = buildModules(reportContext)

        val filterProjectData: ProjectData = IntellijDeltaCoverageLoader.getDeltaProjectData(
            binaryReports,
            modules,
            reportContext.codeUpdateInfo
        )

        val deltaReportLoadStrategy = sequenceOf(
            NamedReportLoadStrategy(
                reportContext.deltaCoverageConfig.reportName,
                ReportBound.DELTA_REPORT,
                PreloadedCoverageReportLoadStrategy(filterProjectData, binaryReports, modules),
            )
        )
        return if (reportContext.deltaCoverageConfig.reportsConfig.fullCoverageReport) {
            deltaReportLoadStrategy + NamedReportLoadStrategy(
                FULL_COVERAGE_REPORT,
                ReportBound.FULL_REPORT,
                ReportLoadStrategy.RawReportLoadStrategy(binaryReports, modules, Filters.EMPTY)
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

    private fun buildModules(reportContext: ReportContext): List<Module> {
        return listOf(
            Module(
                reportContext.deltaCoverageConfig.classFiles.toList(),
                reportContext.deltaCoverageConfig.sourceFiles.toList()
            )
        )
    }

    private class PreloadedCoverageReportLoadStrategy(
        private val coverageData: ProjectData,
        binaryReports: List<BinaryReport>,
        modules: List<Module>
    ) : ReportLoadStrategy(binaryReports, modules) {

        override fun loadProjectData(): ProjectData = coverageData
    }

}
