package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.gradle.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.ReportsConfiguration
import io.github.surpsg.deltacoverage.gradle.ViolationRules
import io.github.surpsg.deltacoverage.gradle.config.ConfigMapper
import io.github.surpsg.deltacoverage.gradle.sources.filter.ClassesFilter
import io.github.surpsg.deltacoverage.gradle.task.internal.GradleReportGenerator
import io.github.surpsg.deltacoverage.report.DeltaReportFacadeFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
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

    @Input
    val coverageEngine: Property<CoverageEngine> = objectFactory.property(CoverageEngine::class.java)

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

    @get:Input
    val includeClasses: ListProperty<String> = objectFactory.listProperty(String::class.java)
        .convention(emptyList())

    @get:Input
    val excludeClasses: ListProperty<String> = objectFactory.listProperty(String::class.java)
        .convention(emptyList())

    @Nested
    val violationRules: Property<ViolationRules> = objectFactory.property(ViolationRules::class.java)

    @Nested
    val reports: Property<ReportsConfiguration> = objectFactory.property(ReportsConfiguration::class.java)
//
//    @get:Input
//    val explainEnabled: Property<Boolean> = objectFactory.property(Boolean::class.java)
//        .convention(project.hasProperty(EXPLAIN_PROPERTY))
//
//    @get:Input
//    val explainOnlyEnabled: Property<Boolean> = objectFactory.property(Boolean::class.java)
//        .convention(project.hasProperty(EXPLAIN_ONLY_PROPERTY))

    @get:OutputDirectory
    val reportsDir: DirectoryProperty = objectFactory.directoryProperty()
        .convention(
            project.layout.buildDirectory.map { it.dir("reports/$BASE_COVERAGE_REPORTS_DIR") }
        )

    private val rootProjectDirProperty: File = project.rootProject.projectDir

    @TaskAction
    fun executeAction() {
        sequenceOf(
//            explainReport(),
            coverageReportsGenerator(),
        ).forEach(GradleReportGenerator::generateReport)
    }

//    private fun explainReport(): GradleReportGenerator =
//        if (explainEnabled.get() || explainOnlyEnabled.get()) {
//            ViewExplainReportGenerator(
//                view = viewName.get(),
//                outputDir = reportsDir.asFile.get(),
//                gradleConfig = deltaCoverageConfigProperty.get(),
//                rootProject = project.rootProject,
//                resolvedSources = ResolvedViewSources(
//                    sources = sourcesFiles.get().files,
//                    classes = classesFiles.get().files,
//                    coverageBinaries = coverageBinaryFiles.get().files,
//                )
//            )
//        } else {
//            GradleReportGenerator.NOOP
//        }

    private fun coverageReportsGenerator(): GradleReportGenerator {
        return object : GradleReportGenerator {
            val diffSource: DiffSource = obtainDiffSource(reportsDir.asFile.get())

            val deltaCoverageConfig: DeltaCoverageConfig = buildDeltaCoverageConfig(
                diffSource,
                classesRoots.get(),
            )

            override fun generateReport() = DeltaReportFacadeFactory
                .buildFacade(deltaCoverageConfig.coverageEngine)
                .generateReports(deltaCoverageConfig)
        }
    }

    private fun obtainDiffSource(
        reportDir: File,
    ): DiffSource = ConfigMapper.convertToDiffSource(
        rootProjectDirProperty,
        TODO(),
//        gradleCoverageConfig.diffSource,
    ).apply {
        val savedFile = saveDiffTo(reportDir)
        log.info("Diff content saved to file://{}", savedFile.absolutePath)
    }

    private fun buildDeltaCoverageConfig(
        diffSource: DiffSource,
        classesRoots: FileCollection,
    ): DeltaCoverageConfig = ConfigMapper.buildCoreConfig(
        deltaCoverageTask = this,
        diffSource = diffSource, // todo

        excludeClassesPatterns = excludeClasses.get().toSet(),
        classesRoots = classesRoots.files,
        classesFiles = ClassesFilter.build {
            include(includeClasses.get())
            exclude(excludeClasses.get())
        }.filter(classesFiles.get()).files,
    )

    companion object {
        const val BASE_COVERAGE_REPORTS_DIR = "coverage-reports"
        const val EXPLAIN_PROPERTY = "explain"
        const val EXPLAIN_ONLY_PROPERTY = "explainOnly"
        private val log: Logger = LoggerFactory.getLogger(DeltaCoverageTask::class.java)
    }
}
