package io.github.surpsg.deltacoverage.report.intellij.report

import com.intellij.rt.coverage.report.Reporter
import io.github.surpsg.deltacoverage.config.ReportsConfig

internal abstract class ReportBuilder(
    protected val reporter: Reporter,
    val reportBound: ReportBound,
    protected val reportsConfig: ReportsConfig
) {

    abstract fun buildReport()
}
