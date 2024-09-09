package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.diff.CodeUpdateInfo
import io.github.surpsg.deltacoverage.diff.parse.ClassFile
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.ReportContext
import io.github.surpsg.deltacoverage.report.jacoco.filters.ModifiedLinesFilter
import io.github.surpsg.deltacoverage.report.jacoco.report.JacocoReport
import io.github.surpsg.deltacoverage.report.jacoco.report.VerifiableReportVisitor
import io.github.surpsg.deltacoverage.report.jacoco.verification.DefaultCoverageRulesVisitor
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore

internal class DeltaCoverageAnalyzableReport(
    private val coverageRulesConfig: CoverageRulesConfig,
    private val jacocoDeltaReport: JacocoDeltaReport,
    private val reportContext: ReportContext,
    jacocoReports: List<JacocoReport>,
) : FullCoverageAnalyzableReport(jacocoReports) {

    override fun buildVisitor(): VerifiableReportVisitor {
        return VerifiableReportVisitor.create(
            reportVisitors(),
            DefaultCoverageRulesVisitor.create(reportContext),
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

    private fun createViolationCheckVisitor(
        rules: List<Rule>
    ): IReportVisitor {
        val violationsOutputResolver = ViolationsOutputResolver(coverageRulesConfig)
        val coverageViolationsPropagator = CoverageViolationsPropagator()

        class CoverageRulesVisitor(
            rulesCheckerVisitor: IReportVisitor
        ) : IReportVisitor by rulesCheckerVisitor {

            override fun visitEnd() {
                val violations: List<String> = violationsOutputResolver.getViolations()
                coverageViolationsPropagator.propagate(coverageRulesConfig, violations)
            }
        }

        return RulesChecker()
            .apply { setRules(rules) }
            .createVisitor(violationsOutputResolver)
            .let { CoverageRulesVisitor(it) }
    }

    override val reportBound: ReportBound = ReportBound.DELTA_REPORT
}
