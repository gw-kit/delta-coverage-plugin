package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.data.ProjectData
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.ReportBound

internal object CoverageAssertion {

    fun verify(
        view: String,
        projectData: ProjectData,
        coverageRulesConfig: CoverageRulesConfig
    ): CoverageSummary {
        val summaryAgg: CoverageSummaryAgg = IntellijVerifierFactory
            .buildVerifiers(projectData, coverageRulesConfig)
            .map { coverageVerifier -> coverageVerifier.verify() }
            .map { verifiedCoverage ->
                CoverageSummaryAgg(
                    coverageInfo = verifiedCoverage.info,
                    verificationResults = verifiedCoverage.buildVerifications(),
                )
            }
            .fold(CoverageSummaryAgg.EMPTY) { acc, summary ->
                acc mergeWith CoverageSummaryAgg(
                    summary.coverageInfo,
                    summary.verificationResults
                )
            }

        return CoverageSummary(
            view = view,
            reportBound = ReportBound.DELTA_REPORT,
            coverageRulesConfig = coverageRulesConfig,
            coverageInfo = summaryAgg.coverageInfo,
            verifications = summaryAgg.verificationResults,
        )
    }

    private fun CoverageVerifier.VerifiedCoverage.buildVerifications(): List<CoverageSummary.VerificationResult> {
        return violations.map { violation ->
            CoverageSummary.VerificationResult(
                coverageEntity = violation.coverageEntity,
                violation = violation.buildCoverageViolatedMessage(),
            )
        }
    }

    private fun CoverageVerifier.Violation.buildCoverageViolatedMessage(): String {
        return "$coverageEntity: expectedMin=$expectedMinValue, actual=$actualValue"
    }

    private data class CoverageSummaryAgg(
        val coverageInfo: List<CoverageSummary.Info>,
        val verificationResults: List<CoverageSummary.VerificationResult>,
    ) {
        infix fun mergeWith(other: CoverageSummaryAgg): CoverageSummaryAgg {
            return CoverageSummaryAgg(
                coverageInfo = coverageInfo + other.coverageInfo,
                verificationResults = verificationResults + other.verificationResults,
            )
        }

        companion object {
            val EMPTY = CoverageSummaryAgg(emptyList(), emptyList())
        }
    }
}
