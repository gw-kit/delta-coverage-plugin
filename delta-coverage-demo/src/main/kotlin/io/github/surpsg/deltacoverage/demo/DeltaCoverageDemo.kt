package io.github.surpsg.deltacoverage.demo

import io.github.surpsg.deltacoverage.cli.config.ConfigLoader
import io.github.surpsg.deltacoverage.cli.report.CliReportRunner
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
        ConfigLoader.loadFromFile(configFile)
    } else {
        // Load from classpath resource
        val resourceStream = object {}.javaClass.getResourceAsStream("/demo.yaml")
        if (resourceStream == null) {
            logger.error("Default config file not found in classpath resources")
            println("Please create delta-coverage-demo/src/main/resources/demo.yaml")
            return
        }
        logger.info("Loading config from classpath: /demo.yaml")
        ConfigLoader.loadFromStream(resourceStream)
    }

    logger.info("Configuration loaded successfully")
    logger.debug("Config details: coverageEngine=${config.coverageEngine}, viewName=${config.viewName}")

    println("Starting delta coverage analysis...")
    println("Coverage engine: ${config.coverageEngine}")
    println("Diff source: ${config.diffSourceFile}")
    println("Working directory: ${File(".").absolutePath}")

    CliReportRunner().run(config)

    logger.info("Delta coverage analysis completed successfully")
    println("\nDelta coverage analysis completed successfully!")
    println("Reports generated to: ${config.reports.reportDir}")
}

private fun printUsageExample() {
    println(
        """
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
    """.trimMargin()
    )
}
