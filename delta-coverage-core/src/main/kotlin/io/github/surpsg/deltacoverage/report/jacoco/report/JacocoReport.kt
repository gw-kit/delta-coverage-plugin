package io.github.surpsg.deltacoverage.report.jacoco.report

import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportType

data class JacocoReport(
    val reportType: ReportType,
    val reportBound: ReportBound,
    val reportsConfig: ReportsConfig,
    val coverageRulesConfig: CoverageRulesConfig,
)
