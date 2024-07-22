package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.report.intellij.IntellijDeltaReportGeneratorFacade
import io.github.surpsg.deltacoverage.report.jacoco.JacocoDeltaReportGeneratorFacade

object DeltaReportFacadeFactory {

    fun buildFacade(
        coverageEngine: CoverageEngine,
    ): DeltaReportGeneratorFacade = when (coverageEngine) {
        CoverageEngine.JACOCO -> JacocoDeltaReportGeneratorFacade()
        CoverageEngine.INTELLIJ -> IntellijDeltaReportGeneratorFacade()
    }
}
