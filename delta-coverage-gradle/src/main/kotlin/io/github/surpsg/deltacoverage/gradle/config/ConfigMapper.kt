package io.github.surpsg.deltacoverage.gradle.config

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.DiffSourceConfig
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.gradle.DiffSourceConfiguration
import io.github.surpsg.deltacoverage.gradle.ViolationRules
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTask
import java.io.File
import io.github.surpsg.deltacoverage.gradle.CoverageEntity as GradleCoverageEntity
import io.github.surpsg.deltacoverage.gradle.ViolationRule as GradleViolationRule

internal object ConfigMapper {

    fun convertToDiffSource(
        projectRoot: File,
        diffSourceConfig: DiffSourceConfiguration
    ): DiffSource {
        val diffConfig = DiffSourceConfig {
            if (diffSourceConfig.git.useNativeGit.get()) {
                file = diffSourceConfig.git.nativeGitDiffFile.get().asFile.absolutePath
            } else {
                file = diffSourceConfig.file.get()
                url = diffSourceConfig.url.get()
                diffBase = diffSourceConfig.git.diffBase.get()
            }
        }
        return DiffSource.buildDiffSource(projectRoot, diffConfig)
    }

    @Suppress("LongParameterList")
    fun buildCoreConfig(
        deltaCoverageTask: DeltaCoverageTask,
        diffSource: DiffSource,
        classesFiles: Set<File>,
        classesRoots: Set<File>,
        excludeClassesPatterns: Set<String>,
    ) = DeltaCoverageConfig {
        coverageEngine = deltaCoverageTask.coverageEngine.get().asCoreEngine()
        this.viewName = deltaCoverageTask.viewName.get()
        this.diffSource = diffSource

        binaryCoverageFiles += deltaCoverageTask.coverageBinaryFiles.get().files
        sourceFiles += deltaCoverageTask.sourcesFiles.get().files
        classFiles += classesFiles
        classRoots += classesRoots
        excludeClasses += excludeClassesPatterns.map { ant -> ant.antToRegex() }

        reportsConfig = ReportsConfig {
            baseReportDir = deltaCoverageTask.reportsDir.asFile.get().absolutePath
            html = ReportConfig {
                outputFileName = "html"
                deltaCoverageTask.reports.get().html.get()
                enabled = deltaCoverageTask.reports.get().html.get()
            }
            xml = ReportConfig {
                outputFileName = "report.xml"
                enabled = deltaCoverageTask.reports.get().xml.get()
            }
            console = ReportConfig {
                outputFileName = "console.txt"
                enabled = deltaCoverageTask.reports.get().console.get()
            }
            markdown = ReportConfig {
                outputFileName = "report.md"
                enabled = deltaCoverageTask.reports.get().markdown.get()
            }
            fullCoverageReport = deltaCoverageTask.reports.get().fullCoverageReport.get()
        }

        coverageRulesConfig = buildCoverageRulesConfig(deltaCoverageTask.violationRules.get())
    }

    private fun buildCoverageRulesConfig(
        rules: ViolationRules,
    ) = CoverageRulesConfig {
        violationRules += rules.rules.get().map { (entity, rule) ->
            buildCoreViolationRule(entity, rule)
        }
        failOnViolation = rules.failOnViolation.get()
    }

    private fun buildCoreViolationRule(
        entity: GradleCoverageEntity,
        rule: GradleViolationRule,
    ): ViolationRule = ViolationRule {
        coverageEntity = entity.mapToCoreCoverageEntity()
        minCoverageRatio = rule.minCoverageRatio.get()
        rule.entityCountThreshold.orNull?.let { threshold ->
            entityCountThreshold = threshold
        }
    }

    private fun GradleCoverageEntity.mapToCoreCoverageEntity(): CoverageEntity = when (this) {
        GradleCoverageEntity.INSTRUCTION -> CoverageEntity.INSTRUCTION
        GradleCoverageEntity.BRANCH -> CoverageEntity.BRANCH
        GradleCoverageEntity.LINE -> CoverageEntity.LINE
    }
}
