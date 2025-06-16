package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.api.Filters
import com.intellij.rt.coverage.report.data.BinaryReport
import com.intellij.rt.coverage.util.ClassNameUtil
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext

internal object ReportLoadStrategyFactory {

    fun buildReportLoadStrategies(reportContext: ReportContext): Sequence<NamedReportLoadStrategy> {
        val binaryReports: List<BinaryReport> = buildBinaryReports(reportContext)
        val intellijSourceInputs = IntellijSourceInputs(
            classesFiles = reportContext.deltaCoverageConfig.classRoots.toList(),
            excludeClasses = reportContext.deltaCoverageConfig.excludeClasses,
            sourcesFiles = reportContext.deltaCoverageConfig.sourceFiles.toList(),
        )

        val buildRawReportLoadStrategy: ReportLoadStrategy =
            buildRawReportLoadStrategy(binaryReports, intellijSourceInputs)
        val data = buildRawReportLoadStrategy.projectData
        val filterProjectData: ProjectData = IntellijDeltaCoverageLoader.getDeltaProjectData(
            data,
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
                buildRawReportLoadStrategy,
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
        Filters(
            emptyList(),
            intellijSourceInputs.excludeClasses.map {
                ClassNameUtil.convertToFQName(it).toPattern()
            },
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
        )
    )
}
