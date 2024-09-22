package io.github.surpsg.deltacoverage.report

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

abstract class DeltaReportGeneratorFacade {

    private val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    fun generateReports(configs: Iterable<DeltaCoverageConfig>) {
        val verificationResults: List<CoverageVerificationResult> = configs.flatMap {
            log.debug("[view:{}] Run Delta-Coverage with config: {}", it.view, it)
            val context = ReportContext(it)
            generate(context)
        }

        CoverageViolationsPropagator().propagateAll(verificationResults)
    }

    internal abstract fun generate(reportContext: ReportContext): List<CoverageVerificationResult>
}
