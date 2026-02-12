package io.github.gwkit.testimpact.gradle.task

import groovy.json.JsonOutput
import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.AnalyzerConfig
import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.JfrTestMappingAnalyzer
import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.TestMappingReport
import io.github.gwkit.testimpact.gradle.sampling.testmapping.report.ConsoleTestMappingReporter
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

    @get:Input
    @get:Optional
    abstract val excludePackages: ListProperty<String>

    @TaskAction
    fun analyze() {
        val testClasses: Set<String> = loadTestClasses()
        if (testClasses.isEmpty()) {
            logger.lifecycle("No test classes found in test-events files")
        } else {
            logger.lifecycle("Loaded {} test classes", testClasses.size)
        }

        val config = AnalyzerConfig(
            includePackages = includePackages.getOrElse(emptyList()),
            excludePackages = excludePackages.getOrElse(emptyList())
        )
        val report: TestMappingReport = JfrTestMappingAnalyzer(config).analyze(jfrFiles.files, testClasses)

        writeReport(report)
    }

    private fun loadTestClasses(): Set<String> = testEventsFiles.files
        .filter { it.exists() }
        .flatMap { it.readLines() }
        .filter { it.isNotBlank() }
        .toSet()

    private fun writeReport(report: TestMappingReport) {
        val file = outputFile.get().asFile
        file.parentFile?.mkdirs()
        file.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(report.toMap())))

        // Print console report
        val consoleReport = ConsoleTestMappingReporter.render(report)
        logger.lifecycle(consoleReport)

        logger.lifecycle("JSON report: file://{}", file.absolutePath)
    }
}
