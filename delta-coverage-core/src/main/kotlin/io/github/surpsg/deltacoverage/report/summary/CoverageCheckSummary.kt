package io.github.surpsg.deltacoverage.report.summary

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.ReportBound
import java.nio.file.Path
import kotlin.io.path.writeText

internal object CoverageCheckSummary {
    private val objectMapper = ObjectMapper()

    fun create(
        reportLocation: Path,
        coverageSummary: CoverageSummary
    ) {
        if (coverageSummary.reportBound == ReportBound.DELTA_REPORT) {
            val writeValueAsString = objectMapper.writeValueAsString(coverageSummary)
            reportLocation.writeText(writeValueAsString)
        }
    }
}
