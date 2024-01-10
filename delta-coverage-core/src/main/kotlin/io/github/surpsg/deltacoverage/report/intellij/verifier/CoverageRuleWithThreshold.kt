package io.github.surpsg.deltacoverage.report.intellij.verifier

import com.intellij.rt.coverage.verify.api.ValueType
import io.github.surpsg.deltacoverage.config.CoverageEntity
import java.math.BigDecimal

internal data class CoverageRuleWithThreshold(
    val id: Int,
    val coverageEntity: CoverageEntity,
    val valueType: ValueType,
    val min: BigDecimal,
    val threshold: Int?
)
