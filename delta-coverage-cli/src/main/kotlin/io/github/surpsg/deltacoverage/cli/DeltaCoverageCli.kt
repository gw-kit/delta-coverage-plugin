package io.github.surpsg.deltacoverage.cli

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.cli.config.CliConfig
import io.github.surpsg.deltacoverage.cli.config.ConfigLoader
import io.github.surpsg.deltacoverage.cli.config.ReportsCliConfig
import io.github.surpsg.deltacoverage.cli.config.ViolationRulesConfig
import io.github.surpsg.deltacoverage.cli.report.CliReportRunner
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File
import java.util.concurrent.Callable
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("DeltaCoverageCli")

@Command(
    name = "delta-coverage",
    mixinStandardHelpOptions = true,
    version = ["delta-coverage-cli 3.6.0"],
    description = ["Computes code coverage of new/modified code based on a provided diff."]
)
class DeltaCoverageCli : Callable<Int> {

    @Option(
        names = ["-c", "--config"],
        description = ["Path to YAML configuration file"]
    )
    var configFile: File? = null

    @Option(
        names = ["-e", "--engine"],
        description = ["Coverage engine: JACOCO or INTELLIJ"]
    )
    var engine: CoverageEngine? = null

    @Option(
        names = ["-f", "--diff-file"],
        description = ["Path to diff file"]
    )
    var diffFile: String? = null

    @Option(
        names = ["--coverage-binary"],
        description = ["Coverage binary files or glob pattern (e.g., 'build/**/jacoco/*.exec')"],
        split = ","
    )
    var coverageBinaryFiles: List<String> = emptyList()

    @Option(
        names = ["--classes"],
        description = ["Class directories or glob pattern (e.g., 'build/classes/**/main')"],
        split = ","
    )
    var classRoots: List<String> = emptyList()

    @Option(
        names = ["-s", "--sources"],
        description = ["Source directories (comma-separated or glob)"],
        split = ","
    )
    var sourceFiles: List<String> = emptyList()

    @Option(
        names = ["--exclude"],
        description = ["Class exclusion patterns (comma-separated)"],
        split = ","
    )
    var excludeClasses: List<String> = emptyList()

    @Option(
        names = ["-o", "--report-dir"],
        description = ["Output directory for reports"]
    )
    var reportDir: String? = null

    @Option(
        names = ["--html"],
        description = ["Generate HTML report"]
    )
    var html: Boolean = false

    @Option(
        names = ["--xml"],
        description = ["Generate XML report"]
    )
    var xml: Boolean = false

    @Option(
        names = ["--console"],
        description = ["Generate console report"]
    )
    var console: Boolean = false

    @Option(
        names = ["--markdown"],
        description = ["Generate markdown report"]
    )
    var markdown: Boolean = false

    @Option(
        names = ["--full-coverage"],
        description = ["Include full coverage in reports"]
    )
    var fullCoverage: Boolean = false

    @Option(
        names = ["--min-coverage"],
        description = ["Minimum coverage ratio (0.0-1.0)"]
    )
    var minCoverage: Double? = null

    @Option(
        names = ["--fail-on-violation"],
        description = ["Exit with error if coverage below threshold"]
    )
    var failOnViolation: Boolean = false

    @Option(
        names = ["--view-name"],
        description = ["Name for the coverage view"]
    )
    var viewName: String? = null

    @Option(
        names = ["-v", "--verbose"],
        description = ["Enable verbose output"]
    )
    var verbose: Boolean = false

    override fun call(): Int {
        return try {
            if (verbose) {
                logger.info("Starting delta coverage analysis...")
            }

            val config = buildConfig()
            validateConfig(config)

            if (verbose) {
                logger.info("Configuration: engine=${config.coverageEngine}, diffFile=${config.diffSourceFile}")
            }

            val runner = CliReportRunner()
            runner.run(config)

            if (verbose) {
                logger.info("Delta coverage analysis completed successfully")
            }

            EXIT_SUCCESS
        } catch (e: ConfigurationException) {
            logger.error("Configuration error: ${e.message}")
            EXIT_CONFIG_ERROR
        } catch (e: CoverageViolationException) {
            logger.error("Coverage violation: ${e.message}")
            EXIT_COVERAGE_VIOLATION
        } catch (e: Exception) {
            logger.error("Runtime error: ${e.message}", e)
            EXIT_RUNTIME_ERROR
        }
    }

    private fun buildConfig(): CliConfig {
        val baseConfig = configFile?.let { ConfigLoader.loadFromFile(it) } ?: CliConfig()

        return baseConfig.copy(
            coverageEngine = engine ?: baseConfig.coverageEngine,
            viewName = viewName ?: baseConfig.viewName,
            diffSourceFile = diffFile ?: baseConfig.diffSourceFile,
            coverageBinaryFiles = coverageBinaryFiles.ifEmpty { baseConfig.coverageBinaryFiles },
            classRoots = classRoots.ifEmpty { baseConfig.classRoots },
            sourceFiles = sourceFiles.ifEmpty { baseConfig.sourceFiles },
            excludeClasses = excludeClasses.ifEmpty { baseConfig.excludeClasses },
            reports = buildReportsConfig(baseConfig.reports),
            violationRules = buildViolationRules(baseConfig.violationRules)
        )
    }

    private fun buildReportsConfig(base: ReportsCliConfig): ReportsCliConfig {
        val hasReportFlags = html || xml || console || markdown
        return base.copy(
            reportDir = reportDir ?: base.reportDir,
            html = if (hasReportFlags) html else base.html,
            xml = if (hasReportFlags) xml else base.xml,
            console = if (hasReportFlags) console else base.console,
            markdown = if (hasReportFlags) markdown else base.markdown,
            fullCoverage = if (fullCoverage) true else base.fullCoverage
        )
    }

    private fun buildViolationRules(base: ViolationRulesConfig): ViolationRulesConfig {
        return base.copy(
            minCoverage = minCoverage ?: base.minCoverage,
            failOnViolation = if (failOnViolation) true else base.failOnViolation
        )
    }

    private fun validateConfig(config: CliConfig) {
        val errors = mutableListOf<String>()

        if (config.coverageEngine == null) {
            errors += "Coverage engine is required (--engine JACOCO or --engine INTELLIJ)"
        }
        if (config.diffSourceFile.isNullOrBlank()) {
            errors += "Diff file is required (--diff-file)"
        }
        if (config.coverageBinaryFiles.isEmpty()) {
            errors += "Coverage binary files are required (--coverage-binary)"
        }
        if (config.classRoots.isEmpty()) {
            errors += "Class directories are required (--classes)"
        }
        if (config.sourceFiles.isEmpty()) {
            errors += "Source directories are required (--sources)"
        }

        if (errors.isNotEmpty()) {
            throw ConfigurationException(errors.joinToString("\n"))
        }
    }

    companion object {
        const val EXIT_SUCCESS = 0
        const val EXIT_COVERAGE_VIOLATION = 1
        const val EXIT_CONFIG_ERROR = 2
        const val EXIT_RUNTIME_ERROR = 3
    }
}

class ConfigurationException(message: String) : RuntimeException(message)
class CoverageViolationException(message: String) : RuntimeException(message)

fun main(args: Array<String>) {
    val exitCode = CommandLine(DeltaCoverageCli()).execute(*args)
    exitProcess(exitCode)
}
