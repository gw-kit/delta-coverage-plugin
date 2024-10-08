package io.github.surpsg.deltacoverage.report.jacoco.verification

import io.github.surpsg.deltacoverage.report.CoverageVerificationResult
import io.github.surpsg.deltacoverage.report.ReportContext
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.check.RulesChecker

internal interface CoverageRulesVisitor : IReportVisitor {
    val verificationResults: List<CoverageVerificationResult>
}

internal object NoOpCoverageRulesVisitor : IReportVisitor by MultiReportVisitor(emptyList()), CoverageRulesVisitor {
    override val verificationResults: List<CoverageVerificationResult> = emptyList()
}

internal class DefaultCoverageRulesVisitor(
    private val reportContext: ReportContext,
    private val violationsOutput: ViolationsOutputResolver,
    rulesCheckerReportVisitor: IReportVisitor,
) : IReportVisitor by rulesCheckerReportVisitor, CoverageRulesVisitor {

    private val innerVerificationResults: MutableList<CoverageVerificationResult> = mutableListOf()

    override val verificationResults: List<CoverageVerificationResult>
        get() = innerVerificationResults

    override fun visitEnd() {
        innerVerificationResults += CoverageVerificationResult(
            view = reportContext.deltaCoverageConfig.reportsConfig.view,
            coverageRulesConfig = reportContext.deltaCoverageConfig.coverageRulesConfig,
            violations = violationsOutput.getViolations(),
        )
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

            return DefaultCoverageRulesVisitor(reportContext, violationsOutput, reportVisitor)
        }
    }
}
