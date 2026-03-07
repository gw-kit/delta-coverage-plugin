package io.github.gwkit.testimpact.gradle.task

import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.AnalyzerConfig
import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.JfrTestMappingAnalyzer
import io.github.gwkit.testimpact.gradle.sampling.testmapping.analysis.TestMappingReport
import io.github.gwkit.testimpact.gradle.sampling.testmapping.report.ReportConfig
import io.github.gwkit.testimpact.gradle.sampling.testmapping.report.ReportWriter
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Task that analyzes JFR recordings and generates test-to-code mapping reports.
 */
abstract class TestMappingAnalysisTask : DefaultTask() {

    init {
        group = "verification"
        description = "Analyzes JFR recordings to map tests to code"
    }

    @get:InputFiles
    @get:Optional
    abstract val jfrFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:Optional
    abstract val testEventsFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val includePackages: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val excludePackages: ListProperty<String>

    @get:Input
    abstract val htmlEnabled: Property<Boolean>

    @get:Input
    abstract val flamegraphEnabled: Property<Boolean>

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

        writeReports(report, testClasses)
    }

    private fun loadTestClasses(): Set<String> = testEventsFiles.files
        .filter { it.exists() }
        .flatMap { it.readLines() }
        .filter { it.isNotBlank() }
        .toSet()

    private fun writeReports(report: TestMappingReport, testClasses: Set<String>) {
        val outputDir = outputDirectory.get().asFile

        val reportConfig = ReportConfig(
            outputDir = outputDir,
            html = htmlEnabled.get(),
            flamegraph = flamegraphEnabled.get(),
        )
        val writer = ReportWriter(reportConfig)

        val generatedFiles = writer.write(report, jfrFiles.files, testClasses)
        generatedFiles.forEach { file ->
            logger.lifecycle("Report: file://{}", file.absolutePath)
        }
    }
}
