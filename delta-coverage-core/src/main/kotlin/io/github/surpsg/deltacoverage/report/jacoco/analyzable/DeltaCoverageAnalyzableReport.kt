package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.diff.CodeUpdateInfo
import io.github.surpsg.deltacoverage.diff.parse.ClassFile
import io.github.surpsg.deltacoverage.report.CoverageViolationsPropagator
import io.github.surpsg.deltacoverage.report.JacocoDeltaReport
import io.github.surpsg.deltacoverage.report.ReportBound
import io.github.surpsg.deltacoverage.report.jacoco.ViolationsOutputResolver
import io.github.surpsg.deltacoverage.report.jacoco.filters.ModifiedLinesFilter
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.check.Rule
import org.jacoco.report.check.RulesChecker

internal class DeltaCoverageAnalyzableReport(
    private val coverageRulesConfig: CoverageRulesConfig,
    private val jacocoDeltaReport: JacocoDeltaReport
) : FullCoverageAnalyzableReport(jacocoDeltaReport, coverageRulesConfig) {

    override fun buildVisitor(): IReportVisitor {
        val visitors: MutableList<IReportVisitor> = mutableListOf(super.buildVisitor())

        visitors += createViolationCheckVisitor(
            jacocoDeltaReport.violations.violationRules
        )

        return MultiReportVisitor(visitors)
    }

    override fun buildAnalyzer(
        executionDataStore: ExecutionDataStore,
        coverageVisitor: ICoverageVisitor
    ): Analyzer {
        val codeUpdateInfo: CodeUpdateInfo = jacocoDeltaReport.codeUpdateInfo

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
