package io.github.surpsg.deltacoverage.gradle.task

import groovy.json.JsonOutput
import io.github.surpsg.deltacoverage.gradle.test.sampling.AnalyzerConfig
import io.github.surpsg.deltacoverage.gradle.test.sampling.JfrTestMappingAnalyzer
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task that analyzes JFR recordings and generates test-to-code mapping report.
 */
abstract class TestMappingAnalysisTask : DefaultTask() {

    @get:InputFiles
    @get:Optional
    abstract val jfrFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:Optional
    abstract val testEventsFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val includePackages: ListProperty<String>

    @TaskAction
    fun analyze() {
        val testClasses = loadTestClasses()
        if (testClasses.isEmpty()) {
            logger.lifecycle("No test classes found in test-events files")
        } else {
            logger.lifecycle("Loaded ${testClasses.size} test classes")
        }

        val config = AnalyzerConfig(
            includePackages = includePackages.getOrElse(emptyList())
        )
        val analyzer = JfrTestMappingAnalyzer(config)
        val report = analyzer.analyze(jfrFiles.files, testClasses)

        writeReport(report)
    }

    private fun loadTestClasses(): Set<String> {
        return testEventsFiles.files
            .filter { it.exists() }
            .flatMap { it.readLines() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun writeReport(report: io.github.surpsg.deltacoverage.gradle.test.sampling.TestMappingReport) {
        val file = outputFile.get().asFile
        file.parentFile?.mkdirs()
        file.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(report.toMap())))

        logger.lifecycle("Test mapping analysis complete:")
        logger.lifecycle("  Total tests: ${report.summary.totalTests}")
        logger.lifecycle("  Total methods: ${report.summary.totalMethods}")
        logger.lifecycle("  Total samples: ${report.summary.totalSamples}")
        logger.lifecycle("  Output: ${file.absolutePath}")
    }
}