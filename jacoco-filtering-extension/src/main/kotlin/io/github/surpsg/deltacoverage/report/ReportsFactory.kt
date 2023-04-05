package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.config.ViolationRuleConfig
import io.github.surpsg.deltacoverage.diff.DiffSource
import org.jacoco.core.analysis.ICoverageNode
import org.jacoco.report.check.Limit
import org.jacoco.report.check.Rule
import java.nio.file.Paths

internal fun reportFactory(
    diffSourceConfig: DeltaCoverageConfig,
    diffSource: DiffSource
): Set<FullReport> {
    val reports: Set<Report> = diffSourceConfig.reportsConfig.toReportTypes()

    val violationRule: Rule = buildRule(diffSourceConfig.violationRuleConfig)
    val baseReportDir = Paths.get(diffSourceConfig.reportsConfig.baseReportDir)
    val report: MutableSet<FullReport> = mutableSetOf(
        DiffReport(
            baseReportDir.resolve("deltaCoverage"),
            reports,
            diffSource,
            Violation(
                diffSourceConfig.violationRuleConfig.failOnViolation,
                listOf(violationRule)
            )
        )
    )

    if (diffSourceConfig.reportsConfig.fullCoverageReport) {
        report += FullReport(
            baseReportDir.resolve("fullReport"),
            reports
        )
    }

    return report
}

private fun ReportsConfig.toReportTypes(): Set<Report> = sequenceOf(
    ReportType.HTML to html,
    ReportType.CSV to csv,
    ReportType.XML to xml
).filter { it.second.enabled }.map {
    Report(it.first, it.second.outputFileName)
}.toSet()

private fun buildRule(
    violationRulesOptions: ViolationRuleConfig
): Rule {
    return sequenceOf(
        ICoverageNode.CounterEntity.INSTRUCTION to violationRulesOptions.minInstructions,
        ICoverageNode.CounterEntity.BRANCH to violationRulesOptions.minBranches,
        ICoverageNode.CounterEntity.LINE to violationRulesOptions.minLines
    ).filter {
        it.second > 0.0
    }.map {
        Limit().apply {
            setCounter(it.first.name)
            minimum = it.second.toString()
        }
    }.toList().let {
        Rule().apply {
            limits = it
        }
    }
}
