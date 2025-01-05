package io.github.surpsg.deltacoverage.report.textual

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.report.textual.Coverage.Companion.has

internal data class RawCoverageData private constructor(
    val aClass: String,
    val instr: Coverage,
    val lines: Coverage,
    val branches: Coverage,
) {
    fun merge(other: RawCoverageData) = this.let { thisData ->
        RawCoverageData {
            aClass = thisData.aClass

            thisData.instr.mergeWith(other.instr).let {
                instr(it.covered, it.total)
            }
            thisData.lines.mergeWith(other.lines).let {
                lines(it.covered, it.total)
            }
            thisData.branches.mergeWith(other.branches).let {
                branches(it.covered, it.total)
            }
        }
    }

    internal class Builder {
        lateinit var aClass: String

        private var instr: Coverage? = null
        private var lines: Coverage? = null
        private var branches: Coverage? = null

        fun instr(covered: Int, total: Int) {
            instr = CoverageEntity.INSTRUCTION.has(covered, total)
        }

        fun lines(covered: Int, total: Int) {
            lines = CoverageEntity.LINE.has(covered, total)
        }

        fun branches(covered: Int, total: Int) {
            branches = CoverageEntity.BRANCH.has(covered, total)
        }

        fun build(): RawCoverageData {
            return RawCoverageData(
                aClass = aClass,

                instr = requireNotNull(instr),
                branches = requireNotNull(branches),
                lines = requireNotNull(lines),
            )
        }
    }

    companion object {

        operator fun invoke(initialize: Builder.() -> Unit) =
            Builder().apply(initialize).build()

        fun newBlank(
            customize: Builder.() -> Unit
        ) = RawCoverageData {
            aClass = ""

            instr(0, 0)
            lines(0, 0)
            branches(0, 0)

            customize()
        }
    }
}
