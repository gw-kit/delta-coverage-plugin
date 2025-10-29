package io.github.surpsg.deltacoverage.demo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.DiffSourceConfig
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.report.DeltaReportFacadeFactory
import org.slf4j.LoggerFactory
import java.io.File

private val logger = LoggerFactory.getLogger("DeltaCoverageDemo")

/**
 * Demo application for manual testing and debugging of delta-coverage-core.
 *
 * Usage:
 * 1. Create a demo.yaml file in delta-coverage-demo/src/main/resources/
 * 2. Run: ./gradlew :delta-coverage-demo:run
 *
 * Or provide a custom config file:
 *   ./gradlew :delta-coverage-demo:run --args="path/to/custom.yaml"
 */
fun main(args: Array<String>) {
    logger.info("Delta Coverage Demo starting...")

    val config = if (args.isNotEmpty()) {
        // Load from file path argument
        val configFile = File(args[0])
        if (!configFile.exists()) {
            logger.error("Config file not found: ${configFile.absolutePath}")
            println("Config file not found: ${configFile.absolutePath}")
            println("Please create a config file with the required YAML structure.")
            printUsageExample()
            return
        }
        logger.info("Loading config from file: ${configFile.absolutePath}")
        println("Loading config from: ${configFile.absolutePath}")
        loadConfigFromFile(configFile)
    } else {
        // Load from classpath resource
        val resourceStream = object {}.javaClass.getResourceAsStream("/demo.yaml")
        if (resourceStream == null) {
            logger.error("Default config file not found in classpath resources")
            println("Please create delta-coverage-demo/src/main/resources/demo.yaml")
            return
        }
        logger.info("Loading config from classpath: /demo.yaml")
        loadConfigFromStream(resourceStream)
    }

    logger.info("Configuration loaded successfully")
    logger.debug("Config details: coverageEngine=${config.coverageEngine}, viewName=${config.viewName}")

    println("Starting delta coverage analysis...")
    println("Coverage engine: ${config.coverageEngine}")
    println("Diff source: ${config.diffSourceFile}")
    println("Working directory: ${File(".").absolutePath}")

    runDeltaCoverage(config)

    logger.info("Delta coverage analysis completed successfully")
    println("\nDelta coverage analysis completed successfully!")
    println("Reports generated to: ${config.reports.reportDir}")
}

private fun createYamlMapper(): ObjectMapper {
    return ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())
}

private fun loadConfigFromFile(configFile: File): DemoConfig {
    val mapper = createYamlMapper()
    return mapper.readValue(configFile)
}

private fun loadConfigFromStream(inputStream: java.io.InputStream): DemoConfig {
    val mapper = createYamlMapper()
    return inputStream.use { mapper.readValue(it) }
}

private fun runDeltaCoverage(config: DemoConfig) {
    val deltaReportFacade = DeltaReportFacadeFactory.buildFacade(config.coverageEngine)

    // Resolve relative paths to absolute paths from current working directory
    val diffSourceFile = File(config.diffSourceFile).absolutePath

    // Build diff source config
    val diffSourceConfig = DiffSourceConfig {
        file = diffSourceFile
    }

    // Determine project root (for git-based diffs)
    val projectRoot = File(".").absoluteFile

    val deltaCoverageConfig = DeltaCoverageConfig {
        viewName = config.viewName
        coverageEngine = config.coverageEngine
        diffSource = DiffSource.buildDiffSource(projectRoot, diffSourceConfig)

        reportsConfig = ReportsConfig {
            baseReportDir = File(config.reports.reportDir).absolutePath

            html = ReportConfig {
                enabled = config.reports.html
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

        binaryCoverageFiles += config.coverageBinaryFiles.asFilesPaths()
        sourceFiles += config.sourceFiles.asFilesPaths()

        classRoots += config.classRoots.asFilesPaths()
        classFiles += config.classFiles.asFilesPaths()
        config.excludeClasses?.let(excludeClasses::addAll)
    }

    deltaReportFacade.generateReports(deltaCoverageConfig)
}

private fun Iterable<String>.asFilesPaths() = map { File(it).absoluteFile }

private fun printUsageExample() {
    println("""
        |
        |Example demo.yaml file:
        |
        |coverageEngine: INTELLIJ
        |viewName: demo
        |diffSourceFile: /path/to/file.diff
        |coverageBinaryFiles:
        |  - /path/to/coverage.ic
        |classRoots:
        |  - /path/to/classes/kotlin/main
        |sourceFiles:
        |  - /path/to/src/main/kotlin
        |reportDir: build/reports/delta-coverage-demo
        |reports:
        |  html: true
        |  console: true
        |  markdown: false
        |  fullCoverage: true
        |
    """.trimMargin())
}

data class DemoConfig(
    val coverageEngine: CoverageEngine,
    val viewName: String = "demo",
    val diffSourceFile: String,
    val reports: ReportsConfigJson,
    val coverageBinaryFiles: List<String>,
    val sourceFiles: List<String>,
    val classRoots: List<String>,
    val classFiles: List<String>,
    val excludeClasses: List<String>?,
)

data class ReportsConfigJson(
    val reportDir: String = "build/reports/delta-coverage-demo",
    val html: Boolean = true,
    val console: Boolean = true,
    val markdown: Boolean = false,
    val fullCoverage: Boolean = true
)
