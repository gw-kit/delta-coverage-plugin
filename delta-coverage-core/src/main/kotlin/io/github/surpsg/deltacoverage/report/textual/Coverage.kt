package io.github.surpsg.deltacoverage.report.textual

import io.github.surpsg.deltacoverage.config.CoverageEntity

internal data class Coverage private constructor(
    val entity: CoverageEntity,
    val covered: Int,
    val total: Int,
) {
    val ratio: Double
        get() = covered ratioBy total

    private infix fun Int.ratioBy(base: Int): Double = this.toDouble() / base

    fun mergeWith(other: Coverage): Coverage {
        require(this.entity == other.entity)
        return Coverage(
            entity,
            covered = covered + other.covered,
            total = total + other.total,
        )
    }

    companion object {
        fun CoverageEntity.has(covered: Int, total: Int): Coverage =
            Coverage(this, covered = covered, total = total)
    }
}
