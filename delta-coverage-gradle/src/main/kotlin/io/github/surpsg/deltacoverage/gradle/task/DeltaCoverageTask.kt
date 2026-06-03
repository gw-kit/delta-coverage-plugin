package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.gradle.config.ConfigMapper
import io.github.surpsg.deltacoverage.gradle.task.internal.GradleReportGenerator
import io.github.surpsg.deltacoverage.gradle.task.internal.ResolvedViewSources
import io.github.surpsg.deltacoverage.gradle.task.internal.ViewExplainReportGenerator
import io.github.surpsg.deltacoverage.gradle.utils.resolveByPath
import io.github.surpsg.deltacoverage.report.DeltaReportFacadeFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration as GradleDeltaCoverageConfig

@DisableCachingByDefault
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
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val coverageBinaryFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sourcesFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @get:InputFiles
    @get:Classpath
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

        sequenceOf(
            explainReport(),
            coverageReportsGenerator(),
        ).forEach(GradleReportGenerator::generateReport)
    }

    private fun explainReport(): GradleReportGenerator =
        if (explainEnabled.get() || explainOnlyEnabled.get()) {
            ViewExplainReportGenerator(
                view = viewName.get(),
                outputDir = getOutputDir(),
                gradleConfig = deltaCoverageConfigProperty.get(),
                rootProject = project.rootProject,
                resolvedSources = ResolvedViewSources(
                    sources = sourcesFiles.get().files,
                    classes = classesFiles.get().files,
                    coverageBinaries = coverageBinaryFiles.get().files,
                )
            )
        } else {
            GradleReportGenerator.NOOP
        }

    private fun coverageReportsGenerator(): GradleReportGenerator {
        if (explainOnlyEnabled.get()) {
            return GradleReportGenerator.NOOP
        }

        return object : GradleReportGenerator {
            val gradleCoverageConfig: GradleDeltaCoverageConfig = deltaCoverageConfigProperty.get()

            val diffSource: DiffSource = obtainDiffSource(getOutputDir(), gradleCoverageConfig)

            val deltaCoverageConfig: DeltaCoverageConfig = buildDeltaCoverageConfig(
                diffSource,
                classesRoots.get(),
                gradleCoverageConfig,
            )

            override fun generateReport() = DeltaReportFacadeFactory
                .buildFacade(deltaCoverageConfig.coverageEngine)
                .generateReports(deltaCoverageConfig)
        }
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
        private val log: Logger = LoggerFactory.getLogger(DeltaCoverageTask::class.java)
    }
}
