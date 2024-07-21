package io.github.surpsg.deltacoverage.report.jacoco

import io.github.surpsg.deltacoverage.report.CoverageVerificationResult
import io.github.surpsg.deltacoverage.report.DeltaReportGeneratorFacade
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

    override fun generate(reportContext: ReportContext): List<CoverageVerificationResult> {
        val analyzableReports: Set<AnalyzableReport> = analyzableReportFactory(reportContext)

        val execFileLoader = CoverageLoader.loadExecFiles(reportContext.binaryCoverageFiles)

        return analyzableReports.flatMap {
            create(reportContext, execFileLoader, it)
        }
    }

    private fun create(
        reportContext: ReportContext,
        execFileLoader: ExecFileLoader,
        analyzableReport: AnalyzableReport,
    ): List<CoverageVerificationResult> {
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
        return verifiableVisitor.verificationResults
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
