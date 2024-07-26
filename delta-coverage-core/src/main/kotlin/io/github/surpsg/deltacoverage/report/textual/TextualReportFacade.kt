package io.github.surpsg.deltacoverage.report.textual

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.lib.kotlin.applyIf
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportType
import io.github.surpsg.deltacoverage.report.textual.Coverage.Companion.toPercents
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.BRANCHES_H
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.CLASS_H
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.FAILURE_COV_PATTERN
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.HEADERS
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.INDEXED_HEADERS
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.INSTR_H
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.LINES_H
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.MAX_CLASS_COLUMN_LENGTH
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.NA_VALUE
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.PERCENT_MULTIPLIER
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.SHRINK_PLACEHOLDER
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.SUCCESS_COV_PATTERN
import io.github.surpsg.deltacoverage.report.textual.ReportsConstants.TARGET_COV_PATTERN
import io.github.surpsg.deltacoverage.report.textual.TextualReportRenderer.Context
import java.io.OutputStream

internal object TextualReportFacade {

    fun generateReport(
        buildContext: BuildContext,
    ) {
        val rawCoverageData: List<RawCoverageData> = buildContext.coverageDataProvider.obtainData()
        val coverageDataValues: List<List<String>> =
            rawCoverageData
                .sortedByDescending { it.lines.ratio }
                .asSequence()
                .map { it.toValuesCollection(buildContext) }
                .toList()

        TextualReportRendererFactory.getBy(buildContext.reportType).render(
            Context {
                output = buildContext.outputStream
                title = buildTitle(buildContext)
                headers = HEADERS
                footer = buildFooter(rawCoverageData, buildContext)
                rows = coverageDataValues
            }
        )
    }

    private fun buildFooter(
        rawCoverageData: List<RawCoverageData>,
        buildContext: BuildContext,
    ): List<List<String>> {
        val expectedValuesLine = Array(INDEXED_HEADERS.size) { "" }
        sequenceOf(
            BRANCHES_H to CoverageEntity.BRANCH,
            LINES_H to CoverageEntity.LINE,
            INSTR_H to CoverageEntity.INSTRUCTION,
        )
            .filter { (_, entity) -> buildContext.targetCoverage.getValue(entity) > 0 }
            .forEach { (headerName, entity) ->
                val expectedCoveragePercents: Int = buildContext.targetCoverage.getValue(entity)
                val headerIndex = INDEXED_HEADERS.getValue(headerName)
                expectedValuesLine[headerIndex] = TARGET_COV_PATTERN.format(expectedCoveragePercents)
            }

        return listOf(
            rawCoverageData.computeTotal().toValuesCollection(buildContext) { coverage, formatted ->
                val expectedCoveragePercents: Int = buildContext.targetCoverage.getValue(coverage.entity)
                when {
                    expectedCoveragePercents < 1 -> formatted
                    (coverage.ratio.toPercents() < expectedCoveragePercents) -> FAILURE_COV_PATTERN.format(formatted)
                    else -> SUCCESS_COV_PATTERN.format(formatted)
                }
            },
            expectedValuesLine.toList(),
        )
    }

    private fun RawCoverageData.toValuesCollection(
        buildContext: BuildContext,
        decorateCoverageValue: (Coverage, String) -> String = { _, formatted -> formatted },
    ): List<String> = INDEXED_HEADERS.keys.map { header ->
        when (header) {
            CLASS_H -> aClass.applyIf(buildContext.shrinkLongClassName) {
                shrinkClassName(MAX_CLASS_COLUMN_LENGTH)
            }

            LINES_H -> decorateCoverageValue(lines, lines.ratio.formatToPercentage())

            BRANCHES_H -> {
                if (branches.total == 0) {
                    NA_VALUE
                } else {
                    decorateCoverageValue(branches, branches.ratio.formatToPercentage())
                }
            }

            INSTR_H -> decorateCoverageValue(instr, instr.ratio.formatToPercentage())


            else -> error("Unknown header: $header")
        }
    }

    private fun List<RawCoverageData>.computeTotal(): RawCoverageData = fold(
        RawCoverageData.newBlank { aClass = "Total" },
        RawCoverageData::merge,
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

    private fun String.shrinkClassName(maxLength: Int): String {
        return if (length > maxLength) {
            val startIndex = length - maxLength + SHRINK_PLACEHOLDER.length
            val keepRange = startIndex until length
            SHRINK_PLACEHOLDER + substring(keepRange)
        } else {
            this
        }
    }

    private fun buildTitle(buildContext: BuildContext): String = when (buildContext.reportBound) {
        ReportBound.DELTA_REPORT -> "Delta Coverage Stats"
        ReportBound.FULL_REPORT -> "Total Coverage Stats"
    }

    internal class BuildContext private constructor(
        val reportType: ReportType,
        val reportBound: ReportBound,
        val coverageDataProvider: RawCoverageDataProvider,
        val outputStream: OutputStream,
        val shrinkLongClassName: Boolean,
        val targetCoverage: Map<CoverageEntity, Int>,
    ) {

        init {
            val supportedTypes = setOf(ReportType.CONSOLE, ReportType.MARKDOWN)
            require(
                reportType in supportedTypes
            ) {
                "Supports only $supportedTypes types"
            }
        }

        class Builder {
            lateinit var reportType: ReportType
            lateinit var reportBound: ReportBound
            lateinit var coverageDataProvider: RawCoverageDataProvider
            lateinit var outputStream: OutputStream
            var shrinkLongClassName: Boolean = false

            private var targetInstr: TargetCoverage? = null
            private var targetLines: TargetCoverage? = null
            private var targetBranches: TargetCoverage? = null

            fun targetInstr(percents: Int) {
                targetInstr = TargetCoverage(CoverageEntity.INSTRUCTION, percents)
            }

            fun targetLines(percents: Int) {
                targetLines = TargetCoverage(CoverageEntity.LINE, percents)
            }

            fun targetBranches(percents: Int) {
                targetBranches = TargetCoverage(CoverageEntity.BRANCH, percents)
            }

            fun build(): BuildContext {
                return BuildContext(
                    reportType,
                    reportBound,
                    coverageDataProvider,
                    outputStream,
                    shrinkLongClassName,
                    sequenceOf(
                        CoverageEntity.INSTRUCTION to targetInstr,
                        CoverageEntity.LINE to targetLines,
                        CoverageEntity.BRANCH to targetBranches
                    ).map { (entity, targetCov) ->
                        requireNotNull(targetCov) {
                            "Required $entity was null."
                        }
                    }.associate { it.coverageEntity to it.percents }
                )
            }
        }

        companion object {
            operator fun invoke(block: Builder.() -> Unit): BuildContext = Builder().apply(block).build()
        }
    }

    private data class TargetCoverage(
        val coverageEntity: CoverageEntity,
        val percents: Int,
    )
}
