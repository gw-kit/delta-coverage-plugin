package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import java.math.BigDecimal
import java.math.RoundingMode

internal data class CoverageSummary(
    val view: String,
    val reportBound: ReportBound,
    val coverageRulesConfig: CoverageRulesConfig,
    val verifications: List<VerificationResult>,
    val coverageInfo: List<Info>,
) {

    fun contextualViolations(): List<String> =
        verifications.map { violation -> "[$view] ${violation.violation}" }

    internal data class VerificationResult(
        val coverageEntity: CoverageEntity,
        val violation: String,
    )

    data class Info(
        val coverageEntity: CoverageEntity,
        val covered: Int,
        val total: Int,
    ) {
        val percents: Double
            get() {
                val ratio: Double = covered.toDouble() / total
                return (ratio * TO_PERCENTS).roundToTwoDecimalPlaces()
            }

        private fun Double.roundToTwoDecimalPlaces(): Double {
            return if (isNaN()) {
                return 0.0
            } else {
                BigDecimal(this)
                    .setScale(MAX_DIGITS_AFTER_DOT, RoundingMode.HALF_UP)
                    .toDouble()
            }
        }
    }

    private companion object {
        const val MAX_DIGITS_AFTER_DOT = 2
        const val TO_PERCENTS = 100
    }
}
