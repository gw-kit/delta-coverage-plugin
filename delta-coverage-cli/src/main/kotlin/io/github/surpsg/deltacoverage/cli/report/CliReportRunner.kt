package io.github.surpsg.deltacoverage.cli.report

import io.github.surpsg.deltacoverage.cli.CoverageViolationException
import io.github.surpsg.deltacoverage.cli.config.CliConfig
import io.github.surpsg.deltacoverage.cli.config.GlobExpander
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.DiffSourceConfig
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.exception.CoverageViolatedException
import io.github.surpsg.deltacoverage.report.DeltaReportFacadeFactory
import org.slf4j.LoggerFactory
import java.io.File

class CliReportRunner {

    private val logger = LoggerFactory.getLogger(CliReportRunner::class.java)

    fun run(config: CliConfig) {
        val expandedConfig = expandGlobPatterns(config)
        val deltaCoverageConfig = buildDeltaCoverageConfig(expandedConfig)

        logger.info("Running delta coverage analysis...")
        logger.debug("Binary coverage files: {}", deltaCoverageConfig.binaryCoverageFiles)
        logger.debug("Class roots: {}", deltaCoverageConfig.classRoots)
        logger.debug("Source files: {}", deltaCoverageConfig.sourceFiles)

        val facade = DeltaReportFacadeFactory.buildFacade(deltaCoverageConfig.coverageEngine)

        try {
            facade.generateReports(deltaCoverageConfig)
            logger.info("Reports generated to: {}", config.reports.reportDir)
        } catch (e: CoverageViolatedException) {
            if (config.violationRules.failOnViolation) {
                throw CoverageViolationException(e.message ?: "Coverage violations detected")
            }
            logger.warn("Coverage violations detected but --fail-on-violation not set: {}", e.message)
        }
    }

    private fun expandGlobPatterns(config: CliConfig): CliConfig {
        val baseDir = File(".")

        val expandedCoverageBinaries = GlobExpander.expandGlobs(config.coverageBinaryFiles, baseDir)
        val expandedClassRoots = GlobExpander.expandGlobs(config.classRoots, baseDir)
        val expandedClassFiles = GlobExpander.expandGlobs(config.classFiles, baseDir)
        val expandedSourceFiles = GlobExpander.expandGlobs(config.sourceFiles, baseDir)

        logger.debug("Expanded coverage binaries: {} -> {}", config.coverageBinaryFiles, expandedCoverageBinaries)
        logger.debug("Expanded class roots: {} -> {}", config.classRoots, expandedClassRoots)
        logger.debug("Expanded source files: {} -> {}", config.sourceFiles, expandedSourceFiles)

        return config.copy(
            coverageBinaryFiles = expandedCoverageBinaries.map { it.absolutePath },
            classRoots = expandedClassRoots.map { it.absolutePath },
            classFiles = expandedClassFiles.map { it.absolutePath },
            sourceFiles = expandedSourceFiles.map { it.absolutePath }
        )
    }

    private fun buildDeltaCoverageConfig(config: CliConfig): DeltaCoverageConfig {
        val diffSourceConfig = DiffSourceConfig {
            file = config.diffSourceFile!!
        }

        val projectRoot = File(".").absoluteFile

        return DeltaCoverageConfig {
            coverageEngine = config.coverageEngine!!
            viewName = config.viewName
            diffSource = DiffSource.buildDiffSource(projectRoot, diffSourceConfig)

            reportsConfig = ReportsConfig {
                baseReportDir = File(config.reports.reportDir).absolutePath

                html = ReportConfig {
                    enabled = config.reports.html
                }

                xml = ReportConfig {
                    enabled = config.reports.xml
                }

                console = ReportConfig {
                    enabled = config.reports.console
                    outputFileName = "console-report.txt"
                }

                markdown = ReportConfig {
                    enabled = config.reports.markdown
                }

                fullCoverageReport = config.reports.fullCoverage
            }

            coverageRulesConfig = buildCoverageRules(config)

            binaryCoverageFiles += config.coverageBinaryFiles.map { File(it) }
            sourceFiles += config.sourceFiles.map { File(it) }
            classRoots += config.classRoots.map { File(it) }
            classFiles += config.classFiles.map { File(it) }
            config.excludeClasses.let(excludeClasses::addAll)
        }
    }

    private fun buildCoverageRules(config: CliConfig): CoverageRulesConfig {
        return CoverageRulesConfig {
            failOnViolation = config.violationRules.failOnViolation

            config.violationRules.minCoverage?.let { minCov ->
                violationRules += ViolationRule {
                    coverageEntity = CoverageEntity.LINE
                    minCoverageRatio = minCov
                }
                violationRules += ViolationRule {
                    coverageEntity = CoverageEntity.BRANCH
                    minCoverageRatio = minCov
                }
                violationRules += ViolationRule {
                    coverageEntity = CoverageEntity.INSTRUCTION
                    minCoverageRatio = minCov
                }
            }
        }
    }
}
