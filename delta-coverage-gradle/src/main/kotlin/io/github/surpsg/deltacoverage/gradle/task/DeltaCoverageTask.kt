package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.config.ConfigMapper
import io.github.surpsg.deltacoverage.gradle.utils.resolveByPath
import io.github.surpsg.deltacoverage.report.DeltaReportFacadeFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
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
    val classesRoots: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @get:Internal
    val classesFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @Nested
    val deltaCoverageConfigProperty: Property<GradleDeltaCoverageConfig> = objectFactory.property(
        GradleDeltaCoverageConfig::class.java
    )

    @get:Input
    val explainEnabled: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(project.hasProperty(EXPLAIN_PROPERTY))

    @get:Input
    val explainOnlyEnabled: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(project.hasProperty(EXPLAIN_ONLY_PROPERTY))

    private val projectDirProperty: File = project.projectDir

    private val rootProjectDirProperty: File = project.rootProject.projectDir

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
            classesRoots.get(),
            gradleCoverageConfig,
        )

        if (explainEnabled.get() || explainOnlyEnabled.get()) {
            generateExplainReport(outputDir, gradleCoverageConfig, deltaCoverageConfig)
        }
        if (explainOnlyEnabled.get()) {
            return
        }

        DeltaReportFacadeFactory
            .buildFacade(deltaCoverageConfig.coverageEngine)
            .generateReports(deltaCoverageConfig)

    }

    private fun generateExplainReport(
        outputDir: File,
        gradleConfig: GradleDeltaCoverageConfig,
        coreConfig: DeltaCoverageConfig,
    ) {
        val resolvedSources = ResolvedViewSources(
            sources = sourcesFiles.get().files,
            classes = classesFiles.get().files,
            coverageBinaries = coverageBinaryFiles.get().files,
        )

        val reportGenerator = ExplainReportGenerator(
            outputDir = outputDir,
            gradleConfig = gradleConfig,
            rootProject = project.rootProject,
            pluginVersion = resolvePluginVersion(),
            resolvedSources = resolvedSources,
        )

        reportGenerator.generateReports(coreConfig)
//        logger.lifecycle("Delta Coverage explain report generated: file://${outputFile.absolutePath}")
    }

    private fun resolvePluginVersion(): String {
        return DeltaCoveragePlugin::class.java.`package`?.implementationVersion
            ?: UNKNOWN_VERSION
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
        classesRoots: FileCollection,
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
            classesRoots = classesRoots.files,
            coverageBinaryFiles = coverageBinaryFiles.get().files,
        )
    }

    companion object {
        const val BASE_COVERAGE_REPORTS_DIR = "coverage-reports"
        const val EXPLAIN_PROPERTY = "explain"
        const val EXPLAIN_ONLY_PROPERTY = "explainOnly"
        const val EXPLAIN_DIR = "explain"
        private const val UNKNOWN_VERSION = "unknown"
        val log: Logger = LoggerFactory.getLogger(DeltaCoverageTask::class.java)
    }
}

data class ResolvedViewSources(
    val sources: Set<File>,
    val classes: Set<File>,
    val coverageBinaries: Set<File>,
)
