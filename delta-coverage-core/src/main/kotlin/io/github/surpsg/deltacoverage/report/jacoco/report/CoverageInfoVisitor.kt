package io.github.surpsg.deltacoverage.report.jacoco.report

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.report.CoverageSummary
import io.github.surpsg.deltacoverage.report.jacoco.verification.NoOpCoverageRulesVisitor
import org.jacoco.core.analysis.IBundleCoverage
import org.jacoco.report.IReportVisitor
import org.jacoco.report.ISourceFileLocator

internal abstract class CoverageInfoVisitor : IReportVisitor by NoOpCoverageRulesVisitor {

    abstract val coverageSummary: List<CoverageSummary.Info>

    private class DefaultCoverageInfoVisitor : CoverageInfoVisitor() {
        private val innerCoverageInfo: MutableList<CoverageSummary.Info> = mutableListOf()

        override val coverageSummary: List<CoverageSummary.Info>
            get() = innerCoverageInfo

        override fun visitBundle(bundle: IBundleCoverage, locator: ISourceFileLocator?) {
            mapOf(
                CoverageEntity.INSTRUCTION to bundle.instructionCounter,
                CoverageEntity.LINE to bundle.lineCounter,
                CoverageEntity.BRANCH to bundle.branchCounter,
            ).forEach { (entity, counter) ->
                innerCoverageInfo += CoverageSummary.Info(
                    coverageEntity = entity,
                    covered = counter.coveredCount,
                    total = counter.totalCount,
                )
            }
        }
    }

    companion object {
        val NO_OP_VISITOR = object : CoverageInfoVisitor() {
            override val coverageSummary: List<CoverageSummary.Info> = emptyList()
        }

        fun create(): CoverageInfoVisitor = DefaultCoverageInfoVisitor()
    }
}
