package io.github.surpsg.deltacoverage.report.console

import io.github.surpsg.deltacoverage.report.console.asciitable.AsciiTableRenderer
import java.io.OutputStream

internal object ConsoleReportFacade {

    private const val PERCENT_MULTIPLIER = 100
    private const val DELTA_COVERAGE_TITLE = "Delta Coverage Stats"

    private const val NA_VALUE = ""

    private const val SOURCE_H = "Source"
    private const val CLASS_H = "Class"
    private const val LINES_H = "Lines"
    private const val BRANCHES_H = "Branches"
    private val HEADERS = listOf(SOURCE_H, CLASS_H, LINES_H, BRANCHES_H)

    fun generateReport(
        coverageDataProvider: RawCoverageDataProvider,
        outputStream: OutputStream
    ) {
        val rawCoverageData: List<RawCoverageData> = coverageDataProvider.obtainData()
        val coverageDataValues: List<List<String>> =
            rawCoverageData
                .sortedByDescending { it.linesRatio }
                .asSequence()
                .map { it.toValuesCollection() }
                .toList()

        AsciiTableRenderer.render(
            AsciiTableRenderer.Context {
                output = outputStream
                title = DELTA_COVERAGE_TITLE
                headers = HEADERS
                footer = rawCoverageData.computeTotal().toValuesCollection()
                rows = coverageDataValues
            }
        )
    }

    private fun RawCoverageData.toValuesCollection(): List<String> = HEADERS.map { header ->
        when (header) {
            SOURCE_H -> source
            CLASS_H -> aClass
            LINES_H -> linesRatio.formatToPercentage()

            BRANCHES_H -> {
                if (branchesTotal == 0) {
                    NA_VALUE
                } else {
                    branchesRatio.formatToPercentage()
                }
            }

            else -> error("Unknown header: $header")
        }
    }

    private fun List<RawCoverageData>.computeTotal(): RawCoverageData = fold(
        RawCoverageData.newBlank { group = "Total" },
        RawCoverageData::merge
    )

    private fun Double.formatToPercentage(): String {
        val percents = this.toFloat() * PERCENT_MULTIPLIER
        val pattern = if (percents % 1 == 0.0f) {
            "%.0f"
        } else {
            "%.2f"
        }
        return pattern.format(percents) + '%'
    }
}
