package io.github.surpsg.deltacoverage.gradle

import io.github.gwkit.coverjet.gradle.task.CovAgentProperties
import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_COVERAGE_REPORT_EXTENSION
import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_COVERAGE_TASK
import io.github.surpsg.deltacoverage.gradle.sources.lookup.SourceSetsLookup.Companion.MAIN_SOURCE_SET
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal open class DeltaCoverageUnitPlugin : Plugin<Project> {

    private val MUST_RUN_AFTER_DEPS: List<Class<out Task>> = listOf(
        Test::class.java,
        CovAgentProperties::class.java,
    )

    override fun apply(project: Project) = project.pluginManager.withPlugin("java") {
        project.rootProject.extensions.configure<DeltaCoverageConfiguration>(DELTA_COVERAGE_REPORT_EXTENSION) { conf ->
            project.setupProjectViews(conf)
        }
    }

    private fun Project.setupProjectViews(config: DeltaCoverageConfiguration) {
        val classesTask: TaskProvider<Task> = tasks.named(JavaPlugin.CLASSES_TASK_NAME)
        val mainSourceSet: List<SourceSet> = listOfNotNull(
            project.extensions.findByType(SourceSetContainer::class.java)?.getByName(MAIN_SOURCE_SET)
        )
        // TODO: it's automatic views
        tasks.withType(Test::class.java) { task ->
            task.createDeltaCoverageTask(config, classesTask, mainSourceSet)
        }
        // TODO: it's manual views:
    }

    private fun Test.createDeltaCoverageTask(
        config: DeltaCoverageConfiguration,
        classesTask: TaskProvider<Task>,
        mainSourceSet: List<SourceSet>,
    ) {
        val testTask = this
        val viewName = testTask.name
        val taskName = DELTA_COVERAGE_TASK + viewName.capitalize()
        project.tasks.register(taskName, DeltaCoverageTask::class.java) { deltaTask ->
            deltaTask.onlyIf {
                config.maybeView(viewName)?.isEnabled() ?: true
            }
            // Configure dependencies
            deltaTask.dependsOn(classesTask)
            MUST_RUN_AFTER_DEPS.forEach { taskType ->
                deltaTask.mustRunAfter(project.tasks.withType(taskType))
            }

            // Basic config
            deltaTask.viewName.set(viewName)
            deltaTask.coverageEngine.set(config.coverage.engine)

            // Configure sources
            val coverageBinaries = listOfNotNull(testTask.extensions.findByType(JacocoTaskExtension::class.java))
                .mapNotNull { it.destinationFile }
            deltaTask.configureSources(viewName, config, mainSourceSet, coverageBinaries)

            // Configure reports and violations
            deltaTask.reports.set(config.reportConfiguration)
            config.maybeView(viewName)?.let {
                deltaTask.violationRules.set(it.violationRules)
            }
        }
    }

    private fun DeltaCoverageTask.configureSources(
        viewName: String,
        config: DeltaCoverageConfiguration,
        mainSourceSet: List<SourceSet>,
        coverageBinaries: List<File>,
    ) {
        coverageBinaryFiles.set(
            project.files(coverageBinaries)
        )
        sourcesFiles.set(
            project.files(mainSourceSet.flatMap { it.allJava.srcDirs })
        )
        classesRoots.set(
            project.files(mainSourceSet.flatMap { it.output.classesDirs })
        )
        classesFiles.set(
            project.files(mainSourceSet.flatMap { it.output })
        )
        config.maybeView(viewName)?.let {
            includeClasses.addAll(it.includeClasses)
            excludeClasses.addAll(it.excludeClasses)
        }
        excludeClasses.addAll(config.excludeClasses)
    }

    private fun DeltaCoverageConfiguration.maybeView(
        viewName: String
    ): ReportView? = reportViews.findByName(viewName)

    private fun ReportView.isEnabled(): Boolean = enabled.getOrElse(true)

    companion object {
        val log: Logger = LoggerFactory.getLogger(DeltaCoverageUnitPlugin::class.java)
    }
}
