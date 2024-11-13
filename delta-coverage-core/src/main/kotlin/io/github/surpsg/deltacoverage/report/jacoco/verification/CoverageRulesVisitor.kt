package io.github.surpsg.deltacoverage.report.jacoco.verification

import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.ReportContext
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.check.RulesChecker

internal interface CoverageRulesVisitor : IReportVisitor {
    val verificationResults: List<CoverageSummary.VerificationResult>
}

internal object NoOpCoverageRulesVisitor : IReportVisitor by MultiReportVisitor(emptyList()), CoverageRulesVisitor {
    override val verificationResults: List<CoverageSummary.VerificationResult> = emptyList()
}

internal class DefaultCoverageRulesVisitor(
    private val violationsOutput: ViolationsOutputResolver,
    rulesCheckerReportVisitor: IReportVisitor,
) : IReportVisitor by rulesCheckerReportVisitor, CoverageRulesVisitor {

    private val innerVerificationResults: MutableList<CoverageSummary.VerificationResult> = mutableListOf()

    override val verificationResults: List<CoverageSummary.VerificationResult>
        get() = innerVerificationResults

    override fun visitEnd() {
        violationsOutput.getViolations().forEach { (entity, violation) ->
            innerVerificationResults += CoverageSummary.VerificationResult(
                coverageEntity = entity,
                violation = violation,
            )
        }
    }

    companion object {
        fun create(reportContext: ReportContext): DefaultCoverageRulesVisitor {
            val violationsOutput = ViolationsOutputResolver(reportContext.deltaCoverageConfig.coverageRulesConfig)

            val reportVisitor: IReportVisitor = RulesChecker()
                .apply {
                    val rules = JacocoVerifierFactory.buildRules(reportContext.deltaCoverageConfig.coverageRulesConfig)
                    setRules(rules)
                }
                .createVisitor(violationsOutput)

            return DefaultCoverageRulesVisitor(violationsOutput, reportVisitor)
        }
    }
}
