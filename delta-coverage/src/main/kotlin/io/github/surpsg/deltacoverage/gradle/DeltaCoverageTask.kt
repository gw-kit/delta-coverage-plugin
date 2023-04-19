package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.DiffSourceConfig
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.config.ViolationRuleConfig
import io.github.surpsg.deltacoverage.report.ReportGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

open class DeltaCoverageTask @Inject constructor(
    objectFactory: ObjectFactory
) : DefaultTask() {

    init {
        group = "verification"
        description = "Builds coverage report only for modified code"
    }

    @get:Input
    val projectDirProperty: Property<File> = objectFactory.property(File::class.java)

    @get:Input
    val rootProjectDirProperty: Property<File> = objectFactory.property(File::class.java)

    @get:InputFiles
    val jacocoExecFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @get:InputFiles
    val jacocoSourceFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @get:InputFiles
    val jacocoClassesFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @Nested
    val deltaCoverageReport: Property<DeltaCoverageConfiguration> = objectFactory.property(
        DeltaCoverageConfiguration::class.java
    )

    @OutputDirectory
    fun getOutputDir(): File {
        return getReportOutputDir().toFile().apply {
            log.debug(
                "Delta Coverage output dir: $absolutePath, " +
                        "exists=${exists()}, isDir=$isDirectory, canRead=${canRead()}, canWrite=${canWrite()}"
            )
        }
    }

    private val sourcesConfigurator: DeltaCoverageSourcesAutoConfigurator by lazy {
        DeltaCoverageSourcesAutoConfigurator(
            deltaCoverageReport,
            jacocoExecFiles.get(),
            jacocoClassesFiles.get(),
            jacocoSourceFiles.get()
        )
    }

    @TaskAction
    fun executeAction() {
        log.info("DeltaCoverage configuration: $deltaCoverageReport")
        val reportDir: File = getOutputDir().apply {
            val isCreated = mkdirs()
            log.debug("Creating of report dir '$absolutePath' is successful: $isCreated")
        }

        val reportGenerator = ReportGenerator(rootProjectDirProperty.get(), buildDeltaCoverageConfig())
        reportGenerator.saveDiffToDir(reportDir).apply {
            log.info("diff content saved to '$absolutePath'")
        }
        reportGenerator.create()
    }

    private fun getReportOutputDir(): Path {
        return Paths.get(deltaCoverageReport.get().reportConfiguration.baseReportDir).let { path ->
            if (path.isAbsolute) {
                path
            } else {
                projectDirProperty.map { it.toPath().resolve(path) }.get()
            }
        }
    }

    private fun buildDeltaCoverageConfig(): DeltaCoverageConfig {
        val diffCovConfig: DeltaCoverageConfiguration = deltaCoverageReport.get()
        return DeltaCoverageConfig(
            reportName = projectDirProperty.map { it.name }.get(),
            diffSourceConfig = DiffSourceConfig(
                file = diffCovConfig.diffSource.file,
                url = diffCovConfig.diffSource.url,
                diffBase = diffCovConfig.diffSource.git.diffBase
            ),
            reportsConfig = ReportsConfig(
                baseReportDir = getReportOutputDir().toAbsolutePath().toString(),
                html = ReportConfig(enabled = diffCovConfig.reportConfiguration.html, "html"),
                csv = ReportConfig(enabled = diffCovConfig.reportConfiguration.csv, "report.csv"),
                xml = ReportConfig(enabled = diffCovConfig.reportConfiguration.xml, "report.xml"),
                fullCoverageReport = diffCovConfig.reportConfiguration.fullCoverageReport
            ),
            violationRuleConfig = ViolationRuleConfig(
                minBranches = diffCovConfig.violationRules.minBranches,
                minInstructions = diffCovConfig.violationRules.minInstructions,
                minLines = diffCovConfig.violationRules.minLines,
                failOnViolation = diffCovConfig.violationRules.failOnViolation
            ),
            execFiles = sourcesConfigurator.obtainExecFiles().files,
            classFiles = collectClassesToAnalyze(diffCovConfig).files,
            sourceFiles = sourcesConfigurator.obtainSourcesFiles().files
        )
    }

    private fun collectClassesToAnalyze(
        diffCovConfig: DeltaCoverageConfiguration
    ): FileCollection {
        val classesFromConfiguration: FileCollection = sourcesConfigurator.obtainClassesFiles()
        return if (diffCovConfig.excludeClasses.isEmpty()) {
            classesFromConfiguration
        } else {
            return classesFromConfiguration.asFileTree.matching { pattern ->
                pattern.exclude(diffCovConfig.excludeClasses)
            }
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(DeltaCoverageTask::class.java)
    }

}
