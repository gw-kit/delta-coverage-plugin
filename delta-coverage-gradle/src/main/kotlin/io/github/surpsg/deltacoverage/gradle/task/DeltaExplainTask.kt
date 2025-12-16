package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.utils.resolveByPath
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class DeltaExplainTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask() {

    init {
        group = "verification"
        description = "Generates a markdown report explaining the delta coverage configuration"
        outputs.upToDateWhen { false }
    }

    @Nested
    val deltaCoverageConfigProperty: Property<DeltaCoverageConfiguration> = objectFactory.property(
        DeltaCoverageConfiguration::class.java
    )

    @OutputFile
    fun getOutputFile(): File {
        val baseReportDirPath: String = deltaCoverageConfigProperty.get().reportConfiguration.baseReportDir.get()
        return project.projectDir
            .resolveByPath(baseReportDirPath)
            .resolve(DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR)
            .resolve(EXPLAIN_REPORT_FILE_NAME)
    }

    @TaskAction
    fun executeAction() {
        val config = deltaCoverageConfigProperty.get()
        val reportGenerator = ExplainReportGenerator(
            config = config,
            rootProject = project.rootProject,
            pluginVersion = resolvePluginVersion(),
        )

        val reportContent = reportGenerator.generate()
        val outputFile = getOutputFile()
        outputFile.parentFile.mkdirs()
        outputFile.writeText(reportContent)

        logger.lifecycle("Delta Coverage explain report generated: file://${outputFile.absolutePath}")
    }

    private fun resolvePluginVersion(): String {
        return DeltaCoveragePlugin::class.java.`package`?.implementationVersion
            ?: UNKNOWN_VERSION
    }

    companion object {
        const val EXPLAIN_REPORT_FILE_NAME = "delta-coverage-explain.md"
        private const val UNKNOWN_VERSION = "unknown"
    }
}
