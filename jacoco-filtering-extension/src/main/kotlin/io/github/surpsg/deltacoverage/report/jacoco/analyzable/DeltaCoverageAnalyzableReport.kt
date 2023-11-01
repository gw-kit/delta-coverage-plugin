package io.github.surpsg.deltacoverage.report.jacoco.analyzable

import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.diff.CodeUpdateInfo
import io.github.surpsg.deltacoverage.diff.parse.ClassFile
import io.github.surpsg.deltacoverage.report.JacocoDeltaReport
import io.github.surpsg.deltacoverage.report.analyzable.ViolationsOutputResolver
import io.github.surpsg.deltacoverage.report.jacoco.filters.ModifiedLinesFilter
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.check.Rule
import org.jacoco.report.check.RulesChecker
import org.slf4j.LoggerFactory

internal class DeltaCoverageAnalyzableReport(
    private val violationRuleConfig: CoverageRulesConfig,
    private val jacocoDeltaReport: JacocoDeltaReport
) : FullCoverageAnalyzableReport(jacocoDeltaReport) {

    override fun buildVisitor(): IReportVisitor {
        val visitors: MutableList<IReportVisitor> = mutableListOf(super.buildVisitor())

        visitors += createViolationCheckVisitor(
            jacocoDeltaReport.violation.failOnViolation,
            jacocoDeltaReport.violation.violationRules
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
        failOnViolation: Boolean,
        rules: List<Rule>
    ): IReportVisitor {
        val log = LoggerFactory.getLogger("ViolationRules")
        val violationsOutputResolver = ViolationsOutputResolver(violationRuleConfig)

        class CoverageRulesVisitor(
            rulesCheckerVisitor: IReportVisitor
        ) : IReportVisitor by rulesCheckerVisitor {
            override fun visitEnd() {
                val violations = violationsOutputResolver.getViolations()
                log.warn("Fail on violations: $failOnViolation. Found violations: ${violations.size}.")
                if (violations.isNotEmpty() && failOnViolation) {
                    throw Exception(violations.joinToString("\n"))
                }
            }
        }

        return RulesChecker()
            .apply { setRules(rules) }
            .createVisitor(violationsOutputResolver)
            .let { CoverageRulesVisitor(it) }
    }
}
