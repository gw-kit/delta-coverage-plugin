package io.github.surpsg.deltacoverage.report.jacoco

import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.DeltaReportGeneratorFacade
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.jacoco.analyzable.AnalyzableReport
import io.github.surpsg.deltacoverage.report.jacoco.analyzable.analyzableReportFactory
import io.github.surpsg.deltacoverage.report.jacoco.converage.CoverageLoader
import io.github.surpsg.deltacoverage.report.jacoco.report.VerifiableReportVisitor
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.IBundleCoverage
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.DirectorySourceFileLocator
import org.jacoco.report.ISourceFileLocator
import org.jacoco.report.MultiSourceFileLocator

internal class JacocoDeltaReportGeneratorFacade : DeltaReportGeneratorFacade() {

    override fun generate(reportContext: ReportContext): CoverageSummary {
        val analyzableReports: Set<AnalyzableReport> = analyzableReportFactory(reportContext)

        val execFileLoader = CoverageLoader.loadExecFiles(reportContext.binaryCoverageFiles)

        val summaries: Map<ReportBound, CoverageSummary> = analyzableReports
            .asSequence()
            .map { it.reportBound to it }
            .map { (reportBound, analyzableReport) ->
                reportBound to create(reportContext, execFileLoader, analyzableReport)
            }
            .toMap()
        return summaries.getValue(ReportBound.DELTA_REPORT)
    }

    private fun create(
        reportContext: ReportContext,
        execFileLoader: ExecFileLoader,
        analyzableReport: AnalyzableReport,
    ): CoverageSummary {
        val bundleCoverage: IBundleCoverage = analyzeStructure(reportContext) { coverageVisitor ->
            analyzableReport.buildAnalyzer(execFileLoader.executionDataStore, coverageVisitor)
        }

        val verifiableVisitor: VerifiableReportVisitor = analyzableReport.buildVisitor()
        verifiableVisitor.run {
            visitInfo(
                execFileLoader.sessionInfoStore.infos,
                execFileLoader.executionDataStore.contents
            )

            visitBundle(
                bundleCoverage,
                createSourcesLocator(reportContext)
            )

            visitEnd()
        }
        return CoverageSummary(
            reportBound = verifiableVisitor.reportBound,
            view = reportContext.deltaCoverageConfig.view,
            coverageRulesConfig = reportContext.deltaCoverageConfig.coverageRulesConfig,
            verifications = verifiableVisitor.verificationResults,
            coverageInfo = verifiableVisitor.coverageInfo
        )
    }

    private fun analyzeStructure(
        reportContext: ReportContext,
        createAnalyzer: (ICoverageVisitor) -> Analyzer,
    ): IBundleCoverage {
        CoverageBuilder().let { builder ->

            val analyzer = createAnalyzer(builder)

            reportContext.classFiles.forEach { analyzer.analyzeAll(it) }

            return builder.getBundle(reportContext.deltaCoverageConfig.view)
        }
    }

    private fun createSourcesLocator(reportContext: ReportContext): ISourceFileLocator {
        return reportContext.srcFiles.asSequence()
            .map {
                DirectorySourceFileLocator(it, "utf-8", DEFAULT_TAB_WIDTH)
            }
            .fold(MultiSourceFileLocator(DEFAULT_TAB_WIDTH)) { accumulator, sourceLocator ->
                accumulator.apply {
                    add(sourceLocator)
                }
            }
    }

    companion object {
        const val DEFAULT_TAB_WIDTH = 4
    }
}
