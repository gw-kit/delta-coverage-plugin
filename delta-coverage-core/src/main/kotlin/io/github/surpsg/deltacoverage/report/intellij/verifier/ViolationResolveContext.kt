package io.github.surpsg.deltacoverage.report.intellij.verifier

import io.github.surpsg.deltacoverage.config.CoverageEntity

// TODO duplicates jacoco implementation
internal open class ViolationResolveContext(
    val coverageEntity: CoverageEntity?,
    val thresholdCount: Int,
    val totalCount: Long,
) {
    open fun isIgnoredByThreshold(): Boolean {
        return totalCount < thresholdCount
    }

    internal companion object {
        val NO_IGNORE_VIOLATION_CONTEXT = object : ViolationResolveContext(null, -1, -1) {
            override fun isIgnoredByThreshold() = false
        }
    }
}
