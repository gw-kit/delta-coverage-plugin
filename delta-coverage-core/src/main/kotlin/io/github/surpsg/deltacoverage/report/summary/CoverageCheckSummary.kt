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
        coverageSummaries: List<CoverageSummary>
    ) {
        val deltaCoverageSuppress = coverageSummaries.filter { it.reportBound == ReportBound.DELTA_REPORT }
        val writeValueAsString = objectMapper.writeValueAsString(deltaCoverageSuppress)
        reportLocation.writeText(writeValueAsString)
    }
}
