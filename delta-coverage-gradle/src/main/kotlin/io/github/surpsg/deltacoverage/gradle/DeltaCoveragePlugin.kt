package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.autoapply.CoverageEngineAutoApply
import io.github.surpsg.deltacoverage.gradle.reportview.ViewLookup
import io.github.surpsg.deltacoverage.gradle.sources.lookup.KoverPluginSourcesLookup
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTask
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTaskConfigurer
import io.github.surpsg.deltacoverage.gradle.task.NativeGitDiffTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class DeltaCoveragePlugin : Plugin<Project> {

    private val registeredViews = mutableSetOf<String>()

    override fun apply(project: Project) = with(project) {
        extensions.create(
            DELTA_COVERAGE_REPORT_EXTENSION,
            DeltaCoverageConfiguration::class.java,
            objects,
        )

        val deltaTaskForViewConfigurer: (String) -> Unit = deltaTaskForViewConfigurer()

        extensions.configure<DeltaCoverageConfiguration>(DELTA_COVERAGE_REPORT_EXTENSION) { config ->
            CoverageEngineAutoApply().apply(project, config)

            // auto-register views from test tasks
            registerReportViews(config, deltaTaskForViewConfigurer)
        }

        afterEvaluate {
            // Register custom views
            getDeltaConfig().reportViews.names.asSequence()
                .filter { it !in registeredViews }
                .filter { it != DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME }
                .forEach { viewName ->
                    deltaTaskForViewConfigurer(viewName)
                }

            // Finally, register the aggregated view
            deltaTaskForViewConfigurer(DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME)
        }
    }

    private fun Project.deltaTaskForViewConfigurer(): (String) -> Unit {
        val deltaCoverageLifecycleTask: Task = project.tasks.create(DELTA_COVERAGE_TASK)
        val nativeGitDiffTask: TaskProvider<NativeGitDiffTask> = createNativeGitDiffTask()
        val config = getDeltaConfig()

        return { viewName: String ->
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

    private fun Project.registerReportViews(
        config: DeltaCoverageConfiguration,
        onView: (String) -> Unit,
    ) = ViewLookup.lookup(this) { viewName: String ->
        config.reportViews.maybeCreate(viewName)
        onView(viewName)
    }

    private fun Project.createNativeGitDiffTask(): TaskProvider<NativeGitDiffTask> {
        return tasks.register(GIT_DIFF_TASK, NativeGitDiffTask::class.java) { gitDiffTask ->

            val diffSource = getDeltaConfig().diffSource
            gitDiffTask.targetBranch.set(diffSource.git.diffBase)

            diffSource.git.nativeGitDiffFile.set(gitDiffTask.diffFile)
            gitDiffTask.dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        }
    }

    private fun Project.createDeltaCoverageViewTask(
        viewName: String,
        config: DeltaCoverageConfiguration,
    ): DeltaCoverageTask {
        val taskName: String = DELTA_COVERAGE_TASK + viewName.capitalize()
        return project.tasks.create(taskName, DeltaCoverageTask::class.java) { deltaCoverageTask ->
            DeltaCoverageTaskConfigurer.configure(viewName, config, deltaCoverageTask)
        }
    }

    private fun Project.getDeltaConfig(): DeltaCoverageConfiguration = extensions.getByType(
        DeltaCoverageConfiguration::class.java
    )

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
