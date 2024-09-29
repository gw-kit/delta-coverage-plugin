package io.github.surpsg.deltacoverage.report.path

import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.ReportBound
import java.io.File

sealed class ReportPathStrategy(
    private val reportsConfig: ReportsConfig,
) {

    abstract val reportFileName: String

    fun buildReportPath(
        reportBound: ReportBound
    ): File {
        val reportDirNameByReportBound: String = when (reportBound) {
            ReportBound.FULL_REPORT -> FULL_COVERAGE_REPORT_DIR
            ReportBound.DELTA_REPORT -> DELTA_COVERAGE_REPORT_DIR
        }
        return File(reportsConfig.baseReportDir)
            .resolve(reportDirNameByReportBound)
            .resolve(reportsConfig.view)
            .resolve(reportFileName)
    }

    internal class Xml(
        reportsConfig: ReportsConfig,
    ) : ReportPathStrategy(reportsConfig) {

        override val reportFileName: String = reportsConfig.xml.outputFileName
    }

    internal class Html(
        reportsConfig: ReportsConfig,
    ) : ReportPathStrategy(reportsConfig) {

        override val reportFileName: String = reportsConfig.html.outputFileName
    }

    class Console(
        reportsConfig: ReportsConfig
    ) : ReportPathStrategy(reportsConfig) {
        override val reportFileName: String = reportsConfig.console.outputFileName
    }

    class Markdown(
        reportsConfig: ReportsConfig
    ) : ReportPathStrategy(reportsConfig) {
        override val reportFileName: String = reportsConfig.markdown.outputFileName
    }

    companion object {
        const val DELTA_COVERAGE_REPORT_DIR = "delta-coverage"
        const val FULL_COVERAGE_REPORT_DIR = "full-coverage-report"
    }
}
