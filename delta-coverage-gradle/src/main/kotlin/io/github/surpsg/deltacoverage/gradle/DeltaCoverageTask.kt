package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.DiffSourceConfig
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.report.DeltaReportFacadeFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import io.github.surpsg.deltacoverage.gradle.CoverageEntity as GradleCoverageEntity

open class DeltaCoverageTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask() {

    init {
        group = "verification"
        description = "Builds coverage report only for modified code"
    }

    @get:InputDirectory
    val projectDirProperty: DirectoryProperty = objectFactory.directoryProperty()

    @get:InputDirectory
    val rootProjectDirProperty: DirectoryProperty = objectFactory.directoryProperty()

    @get:InputFiles
    val coverageBinaryFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @get:InputFiles
    val sourcesFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @get:InputFiles
    val classesFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @Nested
    val deltaCoverageConfigProperty: Property<DeltaCoverageConfiguration> = objectFactory.property(
        DeltaCoverageConfiguration::class.java
    )

    @OutputDirectory
    fun getOutputDir(): File {
        val reportOutputDir: File = getReportOutputDir()
        if (log.isDebugEnabled) {
            log.debug(
                "Delta Coverage output dir: {}, exists={}, isDir={}, canRead={}, canWrite={}",
                reportOutputDir.absolutePath,
                reportOutputDir.exists(),
                reportOutputDir.isDirectory,
                reportOutputDir.canRead(),
                reportOutputDir.canWrite(),
            )
        }
        return reportOutputDir
    }

    @TaskAction
    fun executeAction() {
        log.info("Delta-Coverage plugin configuration: ${deltaCoverageConfigProperty.get()}")
        val reportDir: File = getOutputDir().apply {
            val isCreated = mkdirs()
            log.debug("Creating of report dir '$absolutePath' is successful: $isCreated")
        }

        val deltaCoverageConfig: DeltaCoverageConfig = buildDeltaCoverageConfig()
        if (log.isDebugEnabled) {
            log.debug("Run Delta-Coverage with config: {}", deltaCoverageConfig)
        }

        DeltaReportFacadeFactory
            .buildFacade(
                rootProjectDirProperty.get().asFile,
                deltaCoverageConfigProperty.get().coverage.engine.get(),
                deltaCoverageConfig
            )
            .saveDiffTo(reportDir) { diffFile ->
                log.info("diff content saved to '${diffFile.absolutePath}'")
            }
            .generateReport()
    }

    private fun getReportOutputDir(): File {
        val baseReportDirPath: String = deltaCoverageConfigProperty.get().reportConfiguration.baseReportDir.get()
        val file = File(baseReportDirPath)
        return if (file.isAbsolute) {
            file
        } else {
            projectDirProperty.get().asFile.resolve(baseReportDirPath)
        }.resolve(BASE_COVERAGE_REPORTS_DIR)
    }

    private fun buildDeltaCoverageConfig(): DeltaCoverageConfig {
        val deltaCovConfig: DeltaCoverageConfiguration = deltaCoverageConfigProperty.get()
        return DeltaCoverageConfig {
            reportName = projectDirProperty.map { it.asFile.name }.get()

            diffSourceConfig = DiffSourceConfig {
                file = deltaCovConfig.diffSource.file.get()
                url = deltaCovConfig.diffSource.url.get()
                diffBase = deltaCovConfig.diffSource.git.diffBase.get()
            }

            binaryCoverageFiles += coverageBinaryFiles.get().files
            sourceFiles += sourcesFiles.get().files
            classFiles += classesFiles.get().files

            reportsConfig = ReportsConfig {
                baseReportDir = getReportOutputDir().absolutePath
                html = ReportConfig {
                    outputFileName = "html"
                    enabled = deltaCovConfig.reportConfiguration.html.get()
                }
                csv = ReportConfig {
                    outputFileName = "report.csv"
                    enabled = deltaCovConfig.reportConfiguration.csv.get()
                }
                xml = ReportConfig {
                    outputFileName = "report.xml"
                    enabled = deltaCovConfig.reportConfiguration.xml.get()
                }
                fullCoverageReport = deltaCovConfig.reportConfiguration.fullCoverageReport.get()
            }

            coverageRulesConfig = buildCoverageRulesConfig(deltaCovConfig)
        }
    }

    private fun buildCoverageRulesConfig(diffCovConfig: DeltaCoverageConfiguration) = CoverageRulesConfig {
        val deltaCoverageConfigRules: ViolationRules = diffCovConfig.violationRules
        violationRules += listOf(
            ViolationRule {
                coverageEntity = CoverageEntity.INSTRUCTION
                minCoverageRatio = deltaCoverageConfigRules.minInstructions.get()
                applyNonNullableThreshold(deltaCoverageConfigRules, GradleCoverageEntity.INSTRUCTION)
            },
            ViolationRule {
                coverageEntity = CoverageEntity.LINE
                minCoverageRatio = deltaCoverageConfigRules.minLines.get()
                applyNonNullableThreshold(deltaCoverageConfigRules, GradleCoverageEntity.LINE)
            },
            ViolationRule {
                coverageEntity = CoverageEntity.BRANCH
                minCoverageRatio = deltaCoverageConfigRules.minBranches.get()
                applyNonNullableThreshold(deltaCoverageConfigRules, GradleCoverageEntity.BRANCH)
            },
        )
        failOnViolation = deltaCoverageConfigRules.failOnViolation.get()
    }

    private fun ViolationRule.Builder.applyNonNullableThreshold(
        deltaCoverageConfigRules: ViolationRules,
        entity: GradleCoverageEntity,
    ) {
        deltaCoverageConfigRules.rules.get()
            .getValue(entity)
            .entityCountThreshold.orNull
            ?.let { instructionThreshold -> entityCountThreshold = instructionThreshold }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(DeltaCoverageTask::class.java)
        const val BASE_COVERAGE_REPORTS_DIR = "coverage-reports"
    }
}
