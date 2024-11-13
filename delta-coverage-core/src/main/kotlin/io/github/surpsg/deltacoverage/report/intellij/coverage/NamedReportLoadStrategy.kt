package io.github.surpsg.deltacoverage.report.intellij.coverage

import com.intellij.rt.coverage.report.ReportLoadStrategy
import io.github.surpsg.deltacoverage.report.ReportBound

internal class NamedReportLoadStrategy(
    val reportBound: ReportBound,
    val reportLoadStrategy: ReportLoadStrategy,
)
