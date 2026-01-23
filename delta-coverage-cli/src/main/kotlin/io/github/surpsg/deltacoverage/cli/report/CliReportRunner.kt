package io.github.surpsg.deltacoverage.cli.report

import io.github.surpsg.deltacoverage.cli.CoverageViolationException
import io.github.surpsg.deltacoverage.cli.config.CliConfig
import io.github.surpsg.deltacoverage.exception.CoverageViolatedException
import io.github.surpsg.deltacoverage.report.DeltaReportFacadeFactory
import org.slf4j.LoggerFactory

internal class CliReportRunner {

    private val logger = LoggerFactory.getLogger(CliReportRunner::class.java)

    fun run(config: CliConfig) {
        val deltaCoverageConfig = config.toCoreConfig()

        logger.info("Running delta coverage analysis...")
        logger.debug("Binary coverage files: {}", deltaCoverageConfig.binaryCoverageFiles)
        logger.debug("Class roots: {}", deltaCoverageConfig.classRoots)
        logger.debug("Source files: {}", deltaCoverageConfig.sourceFiles)

        try {
            val facade = DeltaReportFacadeFactory.buildFacade(deltaCoverageConfig.coverageEngine)
            facade.generateReports(deltaCoverageConfig)
            logger.info("Reports generated to: {}", config.reports.reportDir)
        } catch (e: CoverageViolatedException) {
            if (config.violationRules.failOnViolation) {
                throw CoverageViolationException(e.message ?: "Coverage violations detected")
            }
            logger.warn("Coverage violations detected but --fail-on-violation not set: {}", e.message)
        }
    }
}
