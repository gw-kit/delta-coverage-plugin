package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.report.summary.CoverageCheckSummary
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

abstract class DeltaReportGeneratorFacade {

    private val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    fun generateReports(
        config: DeltaCoverageConfig,
    ) {
        log.debug("[{}] Run Delta-Coverage with config: {}", config.view, config)
        val coverageSummaries: Map<ReportBound, CoverageSummary> = generate(ReportContext(config))

        coverageSummaries.forEach { reportBound, coverageSummary ->
            CoverageCheckSummary.create(
                config.reportsConfig.summaries.getValue(reportBound),
                coverageSummary,
            )
        }

        CoverageViolationsPropagator().propagateAll(
            coverageSummaries.getValue(ReportBound.DELTA_REPORT),
        )
    }

    internal abstract fun generate(reportContext: ReportContext): Map<ReportBound, CoverageSummary>
}
