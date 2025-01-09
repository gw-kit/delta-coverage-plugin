package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.gradle.config.ConfigMapper
import io.github.surpsg.deltacoverage.gradle.utils.resolveByPath
import io.github.surpsg.deltacoverage.report.DeltaReportFacadeFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration as GradleDeltaCoverageConfig

open class DeltaCoverageTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask() {

    init {
        group = "verification"
        description = "Builds coverage report only for modified code"
        outputs.upToDateWhen { false }
    }

    @Input
    val viewName: Property<String> = objectFactory.property(String::class.java)

    @get:InputFiles
    val coverageBinaryFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @get:InputFiles
    val sourcesFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @get:InputFiles
    val classesFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @Nested
    val deltaCoverageConfigProperty: Property<GradleDeltaCoverageConfig> = objectFactory.property(
        GradleDeltaCoverageConfig::class.java
    )

    private val projectDirProperty: File = project.projectDir

    private val rootProjectDirProperty: File = project.rootProject.projectDir

    @OutputFile
    val summaryReportPath: RegularFileProperty = objectFactory.fileProperty().convention {
        getOutputDir().resolve("${viewName.get()}-$SUMMARY_REPORT_FILE_NAME")
    }

    @OutputDirectory
    fun getOutputDir(): File {
        val baseReportDirPath: String = deltaCoverageConfigProperty.get().reportConfiguration.baseReportDir.get()
        return projectDirProperty
            .resolveByPath(baseReportDirPath)
            .resolve(BASE_COVERAGE_REPORTS_DIR)
    }

    @TaskAction
    fun executeAction() {
        val gradleCoverageConfig: GradleDeltaCoverageConfig = deltaCoverageConfigProperty.get()
        log.info("Delta-Coverage plugin configuration: $gradleCoverageConfig")

        val outputDir: File = getOutputDir()
        val diffSource: DiffSource = obtainDiffSource(outputDir, gradleCoverageConfig)
        val deltaCoverageConfig: DeltaCoverageConfig = buildDeltaCoverageConfig(
            diffSource,
            gradleCoverageConfig,
        )

        DeltaReportFacadeFactory
            .buildFacade(gradleCoverageConfig.coverage.engine.get())
            .generateReports(
                summaryReportPath.get().asFile.toPath(),
                deltaCoverageConfig,
            )
    }

    private fun obtainDiffSource(
        reportDir: File,
        gradleCoverageConfig: GradleDeltaCoverageConfig,
    ): DiffSource = ConfigMapper.convertToDiffSource(
        rootProjectDirProperty,
        gradleCoverageConfig.diffSource,
    ).apply {
        val savedFile = saveDiffTo(reportDir)
        log.info("Diff content saved to file://{}", savedFile.absolutePath)
    }

    private fun buildDeltaCoverageConfig(
        diffSource: DiffSource,
        gradleCoverageConfig: GradleDeltaCoverageConfig,
    ): DeltaCoverageConfig {
        val view: String = viewName.get()
        return ConfigMapper.convertToCoreConfig(
            viewName = view,
            reportLocation = getOutputDir(),
            diffSource = diffSource,
            deltaCoverageConfig = gradleCoverageConfig,
            sourcesFiles = sourcesFiles.get().files,
            classesFiles = classesFiles.get().files,
            coverageBinaryFiles = coverageBinaryFiles.get().files,
        )
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(DeltaCoverageTask::class.java)
        const val BASE_COVERAGE_REPORTS_DIR = "coverage-reports"
        const val SUMMARY_REPORT_FILE_NAME = "summary.json"
    }
}
