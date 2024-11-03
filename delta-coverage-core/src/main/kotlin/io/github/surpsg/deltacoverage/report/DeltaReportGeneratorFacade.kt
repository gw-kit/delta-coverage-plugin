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
        configs: Iterable<DeltaCoverageConfig>,
    ) {
        val coverageSummaries: List<CoverageSummary> = configs.flatMap {
            log.debug("[{}] Run Delta-Coverage with config: {}", it.view, it)
            val context = ReportContext(it)
            generate(context)
        }

        CoverageCheckSummary.create(summaryFileLocation, coverageSummaries)
        CoverageViolationsPropagator().propagateAll(coverageSummaries)
    }

    internal abstract fun generate(reportContext: ReportContext): List<CoverageSummary>
}
