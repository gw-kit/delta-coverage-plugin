package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.report.summary.CoverageCheckSummary
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import java.nio.file.Path

abstract class DeltaReportGeneratorFacade {

    private val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    fun generateReports(
        summaryFileLocation: Path,
        config: DeltaCoverageConfig,
    ) {
        log.debug("[{}] Run Delta-Coverage with config: {}", config.view, config)
        val coverageSummary: CoverageSummary = generate(ReportContext(config))

        CoverageCheckSummary.create(summaryFileLocation, coverageSummary)
        CoverageViolationsPropagator().propagateAll(coverageSummary)
    }

    internal abstract fun generate(reportContext: ReportContext): CoverageSummary
}
