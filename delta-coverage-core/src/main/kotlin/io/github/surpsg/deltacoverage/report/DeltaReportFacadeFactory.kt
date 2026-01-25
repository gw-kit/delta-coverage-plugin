package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.report.intellij.IntellijDeltaReportGeneratorFacade
import io.github.surpsg.deltacoverage.report.jacoco.JacocoDeltaReportGeneratorFacade

fun interface FacadeFactory {
    fun buildFacade(coverageEngine: CoverageEngine): DeltaReportGeneratorFacade
}

object DeltaReportFacadeFactory : FacadeFactory {

    override fun buildFacade(
        coverageEngine: CoverageEngine,
    ): DeltaReportGeneratorFacade = when (coverageEngine) {
        CoverageEngine.JACOCO -> JacocoDeltaReportGeneratorFacade()
        CoverageEngine.INTELLIJ -> IntellijDeltaReportGeneratorFacade()
    }
}
