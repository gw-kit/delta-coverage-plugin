package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.ReportView
import io.github.surpsg.deltacoverage.report.ReportGenerator
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import java.io.File

@Suppress("TooManyFunctions")
internal class ExplainReportGenerator(
    private val outputDir: File,
    private val gradleConfig: DeltaCoverageConfiguration,
    private val rootProject: Project,
    private val pluginVersion: String,
    private val resolvedSources: ResolvedViewSources,
) : ReportGenerator {

    private val viewProjectsMap: Map<String, Set<String>> by lazy { buildViewProjectsMap() }

    override fun generateReports(config: DeltaCoverageConfig) {
        val content = generate(config)

        outputDir.resolve("${config.view}-explain-report.md")
            .writeText(content)
    }

    private fun generate(config: DeltaCoverageConfig): String = buildString {

        appendLine("# Delta Coverage Explain Report: `${config.view}`")
        appendLine()

        appendPluginConfiguration(config)
        appendDiffConfiguration(config)
        appendReportsConfiguration(config)
        appendView(config)
        appendEnvironment()
    }

    private fun StringBuilder.appendPluginConfiguration(
        config: DeltaCoverageConfig
    ) {
        appendLine("## Plugin Configuration")
        appendLine()
        appendLine("- Plugin version: $pluginVersion")
        appendLine("- Coverage engine: ${config.coverageEngine}")
        appendLine("- Auto-apply coverage plugin: ${gradleConfig.coverage.autoApplyPlugin.get()}")
        appendLine("- Reports output directory: ${config.reportsConfig.baseReportDir}/")
        appendLine()
    }

    private fun StringBuilder.appendDiffConfiguration(
        config: DeltaCoverageConfig
    ) {
        appendLine("## Diff Configuration")
        appendLine()
        appendLine("- Source ${config.diffSource.sourceDescription}")
        appendLine()
    }

    private fun StringBuilder.appendReportsConfiguration(config: DeltaCoverageConfig) {
        appendLine("## Reports Configuration")
        appendLine()
        appendLine("| Report Type | Enabled |")
        appendLine("|-------------|---------|")

        appendLine("| html | ${config.reportsConfig.html.enabled} |")
        appendLine("| xml | ${config.reportsConfig.xml.enabled} |")
        appendLine("| console | ${config.reportsConfig.console.enabled} |")
        appendLine("| markdown | ${config.reportsConfig.markdown.enabled} |")
        appendLine()
        appendLine("- Full coverage report: ${config.reportsConfig.fullCoverageReport}")
        appendLine()
    }

    private fun StringBuilder.appendView(config: DeltaCoverageConfig) {
        appendLine("## '${config.view}' View Details")
        appendLine()

        appendLine("| Property | Value |")
        appendLine("|----------|-------|")

        val isEnabled = gradleConfig.reportViews.getAt(config.view).enabled.getOrElse(true)
        appendLine("| Status | ${if (isEnabled) "enabled" else "disabled"} |")

        val origin = determineViewOrigin(config.view)
        appendLine("| Origin | $origin |")
        appendLine()

        // Projects section
        appendViewProjects(config)

        // Coverage binary files
        appendViewCoverageBinaryFiles()

        // Source directories
        appendViewSourceDirectories()

        // Class directories
        appendViewClassDirectories()

        // Violation rules
        appendViolationRules(config)

        // Filters
        appendFilters(config)
    }

    private fun StringBuilder.appendViewProjects(
        config: DeltaCoverageConfig,
    ) {
        appendLine("**Projects with this view:**")
        gradleConfig.reportViews.getByName(config.view)
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

    private fun StringBuilder.appendViolationRules(
        config: DeltaCoverageConfig,
    ) {
        appendLine("**Violation Rules:**")
        appendLine()
        appendLine("| Metric | Min threshold | Entity count threshold | Enabled |")
        appendLine("|--------|---------------|------------------------|---------|")
        CoverageEntity.entries
            .asSequence()
            .map { it to (config.coverageRulesConfig.entitiesRules[it] ?: ViolationRule.empty(it)) }
            .forEach { (entity, rule) ->
                val minRatio = rule.minCoverageRatio
                val entityThreshold = rule.entityCountThreshold ?: "-"
                val isEnabled = minRatio > 0.0
                appendLine("| ${entity.name.lowercase()} | $minRatio | $entityThreshold | $isEnabled |")
            }
        appendLine()
        appendLine("- Fail on violation: ${config.coverageRulesConfig.failOnViolation}")
        appendLine()
    }

    private fun StringBuilder.appendFilters(
        config: DeltaCoverageConfig,
    ) {
        appendLine("**Filters:**")

        val view: ReportView = gradleConfig.reportViews.getByName(config.view)
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

    private fun buildViewProjectsMap(): Map<String, MutableSet<String>> {
        val result = mutableMapOf<String, MutableSet<String>>()

        rootProject.allprojects.forEach { project ->
            project.tasks.withType(Test::class.java).forEach { testTask ->
                val taskViewName = testTask.name
                val projectPath = project.path.ifEmpty { ":" }
                result.getOrPut(taskViewName) { mutableSetOf() }.add(projectPath)
            }
        }

        // Add aggregated view if it exists
        val aggregatedViewName = DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME
        if (gradleConfig.reportViews.names.contains(aggregatedViewName)) {
            val allProjects = result.values.flatten().toSet()
            if (allProjects.isNotEmpty()) {
                result[aggregatedViewName] = allProjects.toMutableSet()
            }
        }

        return result
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

    private companion object {
        private const val BYTES_IN_KB = 1024L
        private const val BYTES_IN_MB = 1024L * 1024L
    }
}
