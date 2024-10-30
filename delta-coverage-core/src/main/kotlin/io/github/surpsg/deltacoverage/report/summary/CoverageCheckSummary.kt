package io.github.surpsg.deltacoverage.report.summary

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.surpsg.deltacoverage.report.CoverageVerificationResult
import java.nio.file.Path
import kotlin.io.path.writeText

internal object CoverageCheckSummary {
    private val objectMapper = ObjectMapper()

    fun create(
        reportLocation: Path,
        verificationResults: List<CoverageVerificationResult>
    ) {
        val writeValueAsString = objectMapper.writeValueAsString(verificationResults)
        reportLocation.writeText(writeValueAsString)
    }
}
