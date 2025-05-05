package io.github.surpsg.deltacoverage.report.summary

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.surpsg.deltacoverage.report.CoverageSummary
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.writeText

internal object CoverageCheckSummary {
    private val objectMapper = ObjectMapper()

    fun create(
        reportLocation: Path,
        coverageSummary: CoverageSummary,
    ) {
        val writeValueAsString = objectMapper.writeValueAsString(coverageSummary)
        reportLocation.writeText(writeValueAsString)
    }
}
