package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.autoapply.CoverageEngineAutoApply
import io.github.surpsg.deltacoverage.gradle.reportview.ViewLookup
import io.github.surpsg.deltacoverage.gradle.sources.lookup.KoverPluginSourcesLookup
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTask
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTaskConfigurer
import io.github.surpsg.deltacoverage.gradle.task.NativeGitDiffTask
import io.github.surpsg.deltacoverage.gradle.utils.deltaCoverageConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

open class DeltaCoveragePlugin : Plugin<Project> {

    private val registeredViews = ConcurrentHashMap.newKeySet<String>()

    override fun apply(project: Project) = with(project) {
        extensions.create(
            DELTA_COVERAGE_REPORT_EXTENSION,
            DeltaCoverageConfiguration::class.java,
            objects,
        )
        CoverageEngineAutoApply().applyEngine(project)

        val deltaTaskForViewConfigurer: (String) -> Unit = deltaTaskForViewConfigurer()

        extensions.configure<DeltaCoverageConfiguration>(DELTA_COVERAGE_REPORT_EXTENSION) { config ->
            // auto-register views from test tasks
            registerReportViews(config, deltaTaskForViewConfigurer)
        }

        afterEvaluate {
            // Register custom views tasks
            deltaCoverageConfig.reportViews.names.asSequence()
                .filter { it != DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME }
                .filter { it !in registeredViews }
                .forEach { viewName ->
                    deltaTaskForViewConfigurer(viewName)
                }

            deltaCoverageConfig.reportViews.named(DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME) {
                if (!it.enabled.isPresent) {
                    val enabledViewsCount = registeredViews.count { view ->
                        deltaCoverageConfig.reportViews.getByName(view).isEnabled()
                    }
                    it.enabled.set(enabledViewsCount > 1)
                }
            }
            // Finally, register the aggregated view task
            deltaTaskForViewConfigurer(DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME)
        }
    }

    private fun Project.registerReportViews(
        config: DeltaCoverageConfiguration,
        onView: (String) -> Unit,
    ) = ViewLookup.lookup(this) { viewName: String ->
        config.reportViews.maybeCreate(viewName)
        onView(viewName)
    }

    private fun Project.deltaTaskForViewConfigurer(): (String) -> Unit {
        val deltaCoverageLifecycleTask: Task = project.tasks.create(DELTA_COVERAGE_TASK)
        val nativeGitDiffTask: TaskProvider<NativeGitDiffTask> = createNativeGitDiffTask()

        return { viewName: String ->
            val config: DeltaCoverageConfiguration = deltaCoverageConfig
            val deltaTask = createDeltaCoverageViewTask(viewName, config)
            deltaCoverageLifecycleTask.dependsOn(deltaTask)
            afterEvaluate {
                if (config.diffSource.git.useNativeGit.get()) {
                    deltaTask.dependsOn(nativeGitDiffTask)
                }
            }
            registeredViews += viewName
        }
    }

    private fun Project.createDeltaCoverageViewTask(
        viewName: String,
        config: DeltaCoverageConfiguration,
    ): DeltaCoverageTask {
        val taskName: String = DELTA_COVERAGE_TASK + viewName.capitalize()
        return project.tasks.create(taskName, DeltaCoverageTask::class.java) { deltaCoverageTask ->
            DeltaCoverageTaskConfigurer.configure(viewName, config, deltaCoverageTask)
            deltaCoverageTask.onlyIf {
                config.reportViews.getByName(viewName).isEnabled()
            }
        }
    }

    private fun Project.createNativeGitDiffTask(): TaskProvider<NativeGitDiffTask> {
        return tasks.register(GIT_DIFF_TASK, NativeGitDiffTask::class.java) { gitDiffTask ->

            val diffSource = deltaCoverageConfig.diffSource
            gitDiffTask.targetBranch.set(diffSource.git.diffBase)

            diffSource.git.nativeGitDiffFile.set(gitDiffTask.diffFile)
            gitDiffTask.dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        }
    }

    private fun ReportView.isEnabled(): Boolean = enabled.getOrElse(true)

    companion object {
        const val DELTA_COVERAGE_REPORT_EXTENSION = "deltaCoverageReport"
        const val DELTA_COVERAGE_TASK = "deltaCoverage"
        const val GIT_DIFF_TASK = "gitDiff"

        val DELTA_TASK_DEPENDENCIES = setOf(
            JavaPlugin.CLASSES_TASK_NAME,
            KoverPluginSourcesLookup.KOVER_GENERATE_ARTIFACTS_TASK_NAME,
        )
        val log: Logger = LoggerFactory.getLogger(DeltaCoveragePlugin::class.java)
    }
}
