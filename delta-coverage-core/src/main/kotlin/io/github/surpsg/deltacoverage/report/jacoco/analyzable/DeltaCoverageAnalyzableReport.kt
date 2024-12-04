package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.diff.CodeUpdateInfo
import io.github.surpsg.deltacoverage.diff.parse.ClassFile
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.jacoco.filters.ModifiedLinesFilter
import io.github.surpsg.deltacoverage.report.jacoco.report.CoverageInfoVisitor
import io.github.surpsg.deltacoverage.report.jacoco.report.JacocoReport
import io.github.surpsg.deltacoverage.report.jacoco.report.VerifiableReportVisitor
import io.github.surpsg.deltacoverage.report.jacoco.verification.DefaultCoverageRulesVisitor
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore

internal class DeltaCoverageAnalyzableReport(
    private val reportContext: ReportContext,
    jacocoReports: List<JacocoReport>,
) : FullCoverageAnalyzableReport(jacocoReports) {

    override val reportBound: ReportBound = ReportBound.DELTA_REPORT

    override fun buildVisitor(): VerifiableReportVisitor {
        return VerifiableReportVisitor.create(
            ReportBound.DELTA_REPORT,
            reportVisitors(),
            DefaultCoverageRulesVisitor.create(reportContext),
            CoverageInfoVisitor.create(),
        )
    }

    override fun buildAnalyzer(
        executionDataStore: ExecutionDataStore,
        coverageVisitor: ICoverageVisitor
    ): Analyzer {
        val codeUpdateInfo: CodeUpdateInfo = reportContext.codeUpdateInfo

        val classFileFilter: (ClassFile) -> Boolean = {
            codeUpdateInfo.isInfoExists(it)
        }
        return FilteringAnalyzer(executionDataStore, coverageVisitor, classFileFilter) {
            ModifiedLinesFilter(codeUpdateInfo)
        }
    }
}
