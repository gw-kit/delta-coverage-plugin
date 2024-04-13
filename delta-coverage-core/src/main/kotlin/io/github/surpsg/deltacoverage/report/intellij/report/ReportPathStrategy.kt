package io.github.surpsg.deltacoverage.report.intellij.report

import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportType
import java.io.File

sealed class ReportPathStrategy(
    private val reportsConfig: ReportsConfig,
) {

    abstract val reportFileName: String

    fun buildReportPath(reportBound: ReportBound): File {
        return when (reportBound) {
            ReportBound.DELTA_REPORT -> File(reportsConfig.baseReportDir)
                .resolve(DELTA_COVERAGE_REPORT_DIR)
                .resolve(reportFileName)

            ReportBound.FULL_REPORT -> File(reportsConfig.baseReportDir)
                .resolve(FULL_COVERAGE_REPORT_DIR)
                .resolve(reportFileName)
        }
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

    @Deprecated("CSV report is deprecated")
    internal class Csv(
        reportsConfig: ReportsConfig,
    ) : ReportPathStrategy(reportsConfig) {

        override val reportFileName: String = reportsConfig.csv.outputFileName
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
