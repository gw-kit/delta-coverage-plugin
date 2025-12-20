package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig

interface ReportGenerator {
    fun generateReports(config: DeltaCoverageConfig)
}
