package io.github.surpsg.deltacoverage.report.jacoco.report

import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.path.ReportPathStrategy
import java.io.File

internal object ReportPathFactory {

    fun resolveReportAbsolutePath(
        jacocoReport: JacocoReport,
    ): File {
        val reportPathStrategy = when (jacocoReport.reportType) {
            ReportType.HTML -> ReportPathStrategy.Html(jacocoReport.reportsConfig)
            ReportType.XML -> ReportPathStrategy.Xml(jacocoReport.reportsConfig)
            ReportType.CONSOLE -> ReportPathStrategy.Console(jacocoReport.reportsConfig)
            ReportType.MARKDOWN -> ReportPathStrategy.Markdown(jacocoReport.reportsConfig)
        }
        return reportPathStrategy.buildReportPath(jacocoReport.reportBound)
    }
}
