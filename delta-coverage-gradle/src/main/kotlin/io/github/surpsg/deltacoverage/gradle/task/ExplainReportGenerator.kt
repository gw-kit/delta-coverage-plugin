package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.gradle.CoverageEntity
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.ReportView
import io.github.surpsg.deltacoverage.gradle.ViolationRule
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

internal class ExplainReportGenerator(
    private val config: DeltaCoverageConfiguration,
    private val rootProject: Project,
    private val pluginVersion: String,
) {

    private val viewProjectsMap: Map<String, Set<String>> by lazy { buildViewProjectsMap() }

    fun generate(): String = buildString {
        appendLine("# Delta Coverage Explain Report")
        appendLine()

        appendPluginConfiguration()
        appendDiffConfiguration()
        appendProjects()
        appendReportsConfiguration()
        appendViews()
        appendEnvironment()
    }

    private fun StringBuilder.appendPluginConfiguration() {
        appendLine("## Plugin Configuration")
        appendLine()
        appendLine("- Plugin version: $pluginVersion")
        appendLine("- Coverage engine: ${config.coverage.engine.get()}")
        appendLine("- Auto-apply coverage plugin: ${config.coverage.autoApplyPlugin.get()}")
        appendLine("- Reports output directory: ${config.reportConfiguration.baseReportDir.get()}/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}/")
        appendLine()
    }

    private fun StringBuilder.appendDiffConfiguration() {
        appendLine("## Diff Configuration")
        appendLine()

        val diffSource = config.diffSource
        val diffFile = diffSource.file.get()
        val diffUrl = diffSource.url.get()
        val diffBase = diffSource.git.diffBase.get()

        val sourceType = when {
            diffFile.isNotBlank() -> "file"
            diffUrl.isNotBlank() -> "URL"
            diffBase.isNotBlank() -> "git"
            else -> "not configured"
        }

        appendLine("- Source: $sourceType")

        when (sourceType) {
            "file" -> appendLine("- File: $diffFile")
            "URL" -> appendLine("- URL: $diffUrl")
            "git" -> {
                appendLine("- Diff base: $diffBase")
                appendLine("- Use native git: ${diffSource.git.useNativeGit.get()}")
            }
        }
        appendLine()
    }

    private fun StringBuilder.appendProjects() {
        appendLine("## Projects")
        appendLine()

        rootProject.allprojects.sortedBy { it.path }.forEach { project ->
            appendLine("- ${project.path.ifEmpty { ":" }}")
        }
        appendLine()
    }

    private fun StringBuilder.appendReportsConfiguration() {
        appendLine("## Reports Configuration")
        appendLine()
        appendLine("| Report Type | Enabled |")
        appendLine("|-------------|---------|")

        val reportsConfig = config.reportConfiguration
        appendLine("| html | ${reportsConfig.html.get()} |")
        appendLine("| xml | ${reportsConfig.xml.get()} |")
        appendLine("| console | ${reportsConfig.console.get()} |")
        appendLine("| markdown | ${reportsConfig.markdown.get()} |")
        appendLine()
        appendLine("- Full coverage report: ${reportsConfig.fullCoverageReport.get()}")
        appendLine()
    }

    private fun StringBuilder.appendViews() {
        appendLine("## Views")
        appendLine()

        val views = config.reportViews.sortedBy { it.name }
        views.forEach { view ->
            appendView(view)
        }
    }

    private fun StringBuilder.appendView(view: ReportView) {
        val viewName = view.name
        appendLine("### View: `$viewName`")
        appendLine()

        appendLine("| Property | Value |")
        appendLine("|----------|-------|")

        val isEnabled = view.enabled.getOrElse(true)
        appendLine("| Status | ${if (isEnabled) "enabled" else "disabled"} |")

        val origin = determineViewOrigin(viewName)
        appendLine("| Origin | $origin |")

        if (viewName != DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME) {
            val testTasks = viewName
            appendLine("| Test tasks | $testTasks |")
        }

        appendLine()

        // Projects section
        appendViewProjects(viewName)

        // Coverage binary files
        appendViewCoverageBinaryFiles(view)

        // Source directories
        appendViewSourceDirectories(viewName)

        // Class directories
        appendViewClassDirectories(viewName)

        // Violation rules
        appendViolationRules(view)

        // Filters
        appendFilters(view)

        appendLine("---")
        appendLine()
    }

    private fun StringBuilder.appendViewProjects(viewName: String) {
        val projects = viewProjectsMap[viewName] ?: emptySet()
        if (projects.isNotEmpty()) {
            appendLine("**Projects:**")
            projects.sorted().forEach { projectPath ->
                appendLine("- $projectPath")
            }
            appendLine()
        }
    }

    private fun StringBuilder.appendViewCoverageBinaryFiles(view: ReportView) {
        val coverageFiles = view.coverageBinaryFiles
        appendLine("**Coverage Binary Files:**")
        if (coverageFiles != null && !coverageFiles.isEmpty) {
            coverageFiles.files.sortedBy { it.absolutePath }.forEach { file ->
                appendLine("- ${file.absolutePath}")
            }
        } else {
            appendLine("- auto-configured from coverage plugin")
        }
        appendLine()
    }

    private fun StringBuilder.appendViewSourceDirectories(viewName: String) {
        appendLine("**Source Directories:**")
        val projects = viewProjectsMap[viewName] ?: emptySet()
        if (projects.isNotEmpty()) {
            appendLine("- auto-configured from source sets")
        } else {
            val configuredSources = config.sources
            if (configuredSources != null && !configuredSources.isEmpty) {
                configuredSources.files.sortedBy { it.absolutePath }.forEach { file ->
                    appendLine("- ${file.absolutePath}")
                }
            } else {
                appendLine("- auto-configured from source sets")
            }
        }
        appendLine()
    }

    private fun StringBuilder.appendViewClassDirectories(viewName: String) {
        appendLine("**Class Directories:**")
        val projects = viewProjectsMap[viewName] ?: emptySet()
        if (projects.isNotEmpty()) {
            appendLine("- auto-configured from source sets")
        } else {
            val configuredClasses = config.classesDirs
            if (configuredClasses != null && !configuredClasses.isEmpty) {
                configuredClasses.files.sortedBy { it.absolutePath }.forEach { file ->
                    appendLine("- ${file.absolutePath}")
                }
            } else {
                appendLine("- auto-configured from source sets")
            }
        }
        appendLine()
    }

    private fun StringBuilder.appendViolationRules(view: ReportView) {
        appendLine("**Violation Rules:**")
        appendLine()
        appendLine("| Metric | Min threshold | Entity count threshold | Enabled |")
        appendLine("|--------|---------------|------------------------|---------|")

        val violationRules = view.violationRules
        val rules = violationRules.rules.get()

        CoverageEntity.entries.forEach { entity ->
            val rule: ViolationRule? = rules[entity]
            val minRatio = rule?.minCoverageRatio?.get() ?: 0.0
            val entityThreshold = rule?.entityCountThreshold?.orNull?.toString() ?: "-"
            val isEnabled = minRatio > 0.0
            appendLine("| ${entity.name.lowercase()} | $minRatio | $entityThreshold | $isEnabled |")
        }
        appendLine()
        appendLine("- Fail on violation: ${violationRules.failOnViolation.get()}")
        appendLine()
    }

    private fun StringBuilder.appendFilters(view: ReportView) {
        appendLine("**Filters:**")

        val matchClasses = view.matchClasses.get()
        val excludeClasses = config.excludeClasses.get()

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
                val viewName = testTask.name
                val projectPath = project.path.ifEmpty { ":" }
                result.getOrPut(viewName) { mutableSetOf() }.add(projectPath)
            }
        }

        // Add aggregated view if it exists and has more than one non-aggregated view
        val aggregatedViewName = DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME
        if (config.reportViews.names.contains(aggregatedViewName)) {
            val allProjects = result.values.flatten().toSet()
            if (allProjects.isNotEmpty()) {
                result[aggregatedViewName] = allProjects.toMutableSet()
            }
        }

        // Add manually configured views that don't correspond to test tasks
        config.reportViews.names.forEach { viewName ->
            if (viewName !in result && viewName != aggregatedViewName) {
                // Manual view without corresponding test task
                result[viewName] = mutableSetOf()
            }
        }

        return result
    }

    private fun determineViewOrigin(viewName: String): String {
        return when {
            viewName == DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME -> "auto-created"
            viewProjectsMap[viewName]?.isNotEmpty() == true -> "discovered"
            else -> "manual"
        }
    }
}
