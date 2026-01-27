package io.github.surpsg.deltacoverage.cli.config

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.DiffSourceConfig
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.diff.DiffSource
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.collections.plusAssign

internal data class CliConfig(
    val coverageEngine: CoverageEngine = CoverageEngine.JACOCO,
    val viewName: String = "cli",
    val diffSourceFile: String? = null,
    val coverageBinaryFiles: List<String> = emptyList(),
    val classFiles: List<String> = emptyList(),
    val classRoots: List<String> = emptyList(),
    val sourceFiles: List<String> = emptyList(),
    val excludeClasses: List<String> = emptyList(),
    val reports: ReportsCliConfig = ReportsCliConfig(),
    val violationRules: ViolationRulesConfig = ViolationRulesConfig()
) {

    fun toCoreConfig(): DeltaCoverageConfig = expandGlobPatterns().buildCoreConfig()

    private fun expandGlobPatterns(): CliConfig {
        val globExpander = GlobExpander()

        val expandedCoverageBinaries = globExpander.expandGlobs(coverageBinaryFiles)
        val expandedClassRoots = globExpander.expandGlobs(classRoots)
        val expandedClassFiles = globExpander.expandGlobs(classFiles)
        val expandedSourceFiles = globExpander.expandGlobs(sourceFiles)

        logger.debug("Expanded coverage binaries: {} -> {}", coverageBinaryFiles, expandedCoverageBinaries)
        logger.debug("Expanded class roots: {} -> {}", classRoots, expandedClassRoots)
        logger.debug("Expanded source files: {} -> {}", sourceFiles, expandedSourceFiles)

        return copy(
            coverageBinaryFiles = expandedCoverageBinaries.map { it.toAbsolutePath().toString() },
            classRoots = expandedClassRoots.map { it.toAbsolutePath().toString() },
            classFiles = expandedClassFiles.map { it.toAbsolutePath().toString() },
            sourceFiles = expandedSourceFiles.map { it.toAbsolutePath().toString() }
        )
    }

    private fun buildCoreConfig(): DeltaCoverageConfig {
        val config = this
        val diffSourceConfig = DiffSourceConfig {
            file = config.diffSourceFile!!
        }

        val projectRoot = File(".").absoluteFile

        return DeltaCoverageConfig {
            coverageEngine = config.coverageEngine
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

            coverageRulesConfig = config.violationRules.toCoreRules()

            binaryCoverageFiles += config.coverageBinaryFiles.map { File(it) }
            sourceFiles += config.sourceFiles.map { File(it) }
            classRoots += config.classRoots.map { File(it) }
            classFiles += config.classFiles.map { File(it) }
            config.excludeClasses.let(excludeClasses::addAll)
        }
    }

    private fun ViolationRulesConfig.toCoreRules(): CoverageRulesConfig {
        return CoverageRulesConfig {
            failOnViolation = this@toCoreRules.failOnViolation

            minCoverage?.let { minCov ->
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

    data class ReportsCliConfig(
        val reportDir: String = "build/reports/delta-coverage",
        val html: Boolean = true,
        val xml: Boolean = false,
        val console: Boolean = true,
        val markdown: Boolean = false,
        val fullCoverage: Boolean = false
    )

    data class ViolationRulesConfig(
        val minCoverage: Double? = null,
        val failOnViolation: Boolean = false
    )

    private companion object {
        private val logger = LoggerFactory.getLogger(CliConfig::class.java)
    }
}
