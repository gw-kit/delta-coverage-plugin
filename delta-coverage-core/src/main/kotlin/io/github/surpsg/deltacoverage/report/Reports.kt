package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.diff.CodeUpdateInfo
import io.github.surpsg.deltacoverage.report.intellij.report.ReportPathStrategy
import org.jacoco.report.check.Rule
import java.io.File

open class FullReport(
    val jacocoReports: List<JacocoReport>
) {
    fun resolveReportAbsolutePath(jacocoReport: JacocoReport): File {
        val reportPathStrategy = when (jacocoReport.reportType) {
            ReportType.HTML -> ReportPathStrategy.Html(jacocoReport.reportsConfig)
            ReportType.XML -> ReportPathStrategy.Xml(jacocoReport.reportsConfig)
            ReportType.CSV -> ReportPathStrategy.Csv(jacocoReport.reportsConfig)
            ReportType.CONSOLE -> ReportPathStrategy.Console(jacocoReport.reportsConfig)
            ReportType.MARKDOWN -> ReportPathStrategy.Markdown(jacocoReport.reportsConfig)
        }
        println("${jacocoReport.reportType} ${reportPathStrategy}")
        return reportPathStrategy.buildReportPath(jacocoReport.reportBound)
    }
}

internal class JacocoDeltaReport(
    reports: List<JacocoReport>,
    val codeUpdateInfo: CodeUpdateInfo,
    val violations: Violations
) : FullReport(reports)

data class JacocoReport(
    val reportType: ReportType,
    val reportBound: ReportBound,
    val reportsConfig: ReportsConfig
)

@Suppress("MagicNumber")
enum class ReportType(val priority: Int) {
    CONSOLE(1),
    HTML(2),
    MARKDOWN(3),
    XML(4),

    @Deprecated("CSV will be removed soon.")
    CSV(Int.MAX_VALUE),
}

data class Violations(
    val failOnViolation: Boolean,
    val violationRules: List<Rule>
)
