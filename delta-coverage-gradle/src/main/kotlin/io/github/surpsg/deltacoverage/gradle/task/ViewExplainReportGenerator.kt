package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.gradle.CoverageEntity
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.ReportView
import io.github.surpsg.deltacoverage.gradle.config.ConfigMapper
import io.github.surpsg.deltacoverage.gradle.task.internal.GradleReportGenerator
import io.github.surpsg.deltacoverage.gradle.task.internal.ResolvedViewSources
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.invoke.MethodHandles

@Suppress("TooManyFunctions")
internal class ViewExplainReportGenerator(
    private val view: String,
    private val outputDir: File,
    private val gradleConfig: DeltaCoverageConfiguration,
    private val rootProject: Project,
    private val resolvedSources: ResolvedViewSources,
) : GradleReportGenerator {

    override fun generateReport() {
        val content = generate()

        outputDir.resolve("${view}-explain-report.md").apply {
            writeText(content)
            println("[Delta Coverage][$view] explain report: file://${this.absolutePath}")
        }
    }

    private fun generate(): String = buildString {

        appendLine("# Delta Coverage Explain Report: `${view}`")
        appendLine()

        appendPluginConfiguration()
        appendDiffConfiguration()
        appendReportsConfiguration()
        appendView()
        appendEnvironment()
    }

    private fun StringBuilder.appendPluginConfiguration() {
        appendLine("## Plugin Configuration")
        appendLine()
        appendLine("- Plugin version: ${resolvePluginVersion()}")
        appendLine("- Coverage engine: ${gradleConfig.coverage.engine.get()}")
        appendLine("- Auto-apply coverage plugin: ${gradleConfig.coverage.autoApplyPlugin.get()}")
        appendLine("- Reports output directory: ${outputDir}/")
        appendLine()
    }

    private fun StringBuilder.appendDiffConfiguration() {
        val diffSource: DiffSource = ConfigMapper.convertToDiffSource(
            rootProject.projectDir,
            gradleConfig.diffSource,
        )
        appendLine("## Diff Configuration")
        appendLine()
        appendLine("- Source ${diffSource.sourceDescription}")
        appendLine()
    }

    private fun StringBuilder.appendReportsConfiguration() {
        appendLine("## Reports Configuration")
        appendLine()
        appendLine("| Report Type | Enabled |")
        appendLine("|-------------|---------|")

        with(gradleConfig.reportConfiguration) {
            appendLine("| html | ${html.get()} |")
            appendLine("| xml | ${xml.get()} |")
            appendLine("| console | ${console.get()} |")
            appendLine("| markdown | ${markdown.get()} |")
            appendLine()
            appendLine("- Full coverage report: ${fullCoverageReport.get()}")
            appendLine()
        }
    }

    private fun StringBuilder.appendView() {
        appendLine("## '${view}' View Details")
        appendLine()

        appendLine("| Property | Value |")
        appendLine("|----------|-------|")

        val isEnabled = gradleConfig.reportViews.getAt(view).enabled.getOrElse(true)
        appendLine("| Status | ${if (isEnabled) "enabled" else "disabled"} |")

        val origin = determineViewOrigin(view)
        appendLine("| Origin | $origin |")
        appendLine()

        // Projects section
        appendViewProjects()

        // Coverage binary files
        appendViewCoverageBinaryFiles()

        // Source directories
        appendViewSourceDirectories()

        // Class directories
        appendViewClassDirectories()

        // Violation rules
        appendViolationRules()

        // Filters
        appendFilters()
    }

    private fun StringBuilder.appendViewProjects() {
        appendLine("**Projects with this view:**")
        gradleConfig.reportViews.getByName(view)
            .associatedProjects.get()
            .toSortedSet()
            .forEach { projectPath ->
                appendLine("- $projectPath")
            }
    }

    private fun StringBuilder.appendViewCoverageBinaryFiles() {
        appendLine("**Coverage Binary Files:**")

        val files = resolvedSources.coverageBinaries
        if (files.isNotEmpty()) {
            appendLine()
            appendLine("| File | Size | Exists |")
            appendLine("|------|------|--------|")
            files.sortedBy { it.absolutePath }.forEach { file ->
                val exists = file.exists()
                val size = if (exists) formatFileSize(file.length()) else "-"
                appendLine("| ${file.absolutePath} | $size | $exists |")
            }
        } else {
            appendLine("- none resolved")
        }
        appendLine()
    }

    private fun StringBuilder.appendViewSourceDirectories() {
        appendLine("**Source Directories:**")

        val dirs = resolvedSources.sources.filter { it.isDirectory }
        if (dirs.isNotEmpty()) {
            dirs.sortedBy { it.absolutePath }.forEach { dir ->
                val fileCount = dir.walkTopDown().filter { it.isFile }.count()
                appendLine("- ${dir.absolutePath} ($fileCount files)")
            }
        } else {
            appendLine("- none resolved")
        }
        appendLine()
    }

    private fun StringBuilder.appendViewClassDirectories() {
        appendLine("**Class Directories:**")

        val dirs = resolvedSources.classes.filter { it.isDirectory }
        if (dirs.isNotEmpty()) {
            dirs.sortedBy { it.absolutePath }.forEach { dir ->
                val classCount = dir.walkTopDown().filter { it.extension == "class" }.count()
                appendLine("- ${dir.absolutePath} ($classCount .class files)")
            }
        } else {
            appendLine("- none resolved")
        }
        appendLine()
    }

    private fun StringBuilder.appendViolationRules() {
        appendLine("**Violation Rules:**")
        appendLine()
        appendLine("| Metric | Min threshold | Entity count threshold | Enabled |")
        appendLine("|--------|---------------|------------------------|---------|")
        val violationRules = gradleConfig.reportViews.getByName(view).violationRules
        CoverageEntity.entries
            .asSequence()
            .map { entity -> entity to violationRules.rules.getting(entity).get() }
            .forEach { (entity, rule) ->
                val minRatio = rule.minCoverageRatio.get()
                val entityThreshold = rule.entityCountThreshold.orNull ?: "-"
                val isEnabled = minRatio > 0.0
                appendLine("| ${entity.name.lowercase()} | $minRatio | $entityThreshold | $isEnabled |")
            }
        appendLine()
        appendLine("- Fail on violation: ${violationRules.failOnViolation.get()}")
        appendLine()
    }

    private fun StringBuilder.appendFilters() {
        appendLine("**Filters:**")

        val view: ReportView = gradleConfig.reportViews.getByName(view)
        val matchClasses = view.matchClasses.get()
        val excludeClasses = gradleConfig.excludeClasses.get()

        if (matchClasses.isNotEmpty()) {
            appendLine("- Include classes: ${matchClasses.joinToString(", ") { "`$it`" }}")
        } else {
            appendLine("- Include classes: all")
        }

        if (excludeClasses.isNotEmpty()) {
            appendLine("- Exclude classes: ${excludeClasses.joinToString(", ") { "`$it`" }}")
        } else {
            appendLine("- Exclude classes: none")
        }
        appendLine()
    }

    private fun StringBuilder.appendEnvironment() {
        appendLine("## Environment")
        appendLine()
        appendLine("- Gradle version: ${rootProject.gradle.gradleVersion}")
        appendLine("- Java version: ${System.getProperty("java.version")}")
        appendLine("- Java vendor: ${System.getProperty("java.vendor")}")
        appendLine()
    }

    private fun determineViewOrigin(name: String): String = when {
        name == DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME -> "auto-created"
        gradleConfig.reportViews.getAt(name).autoDiscovered.get() == true -> "discovered"
        else -> "manual"
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < BYTES_IN_KB -> "$bytes B"
            bytes < BYTES_IN_MB -> "${bytes / BYTES_IN_KB} KB"
            else -> "${bytes / BYTES_IN_MB} MB"
        }
    }

    private fun resolvePluginVersion(): String {
        return DeltaCoveragePlugin::class.java.`package`?.implementationVersion
            ?: "unknown"
    }

    private companion object {
        private const val BYTES_IN_KB = 1024L
        private const val BYTES_IN_MB = 1024L * 1024L
    }
}
