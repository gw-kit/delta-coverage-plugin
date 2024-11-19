package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.autoapply.CoverageEngineAutoApply
import io.github.surpsg.deltacoverage.gradle.reportview.ViewLookup
import io.github.surpsg.deltacoverage.gradle.sources.lookup.KoverPluginSourcesLookup
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageLifecycleTask
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTask
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTask.Companion.BASE_COVERAGE_REPORTS_DIR
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTaskConfigurer
import io.github.surpsg.deltacoverage.gradle.task.NativeGitDiffTask
import io.github.surpsg.deltacoverage.gradle.utils.resolveByPath
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class DeltaCoveragePlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        extensions.create(
            DELTA_COVERAGE_REPORT_EXTENSION,
            DeltaCoverageConfiguration::class.java,
            objects,
        )

        extensions.configure<DeltaCoverageConfiguration>(DELTA_COVERAGE_REPORT_EXTENSION) { config ->
            CoverageEngineAutoApply().apply(project, config)

            val nativeGitDiffTask = createNativeGitDiffTask(config)
            val deltaCoverageLifecycleTask = project.createDeltaCoverageLifecycle(config)

            project.autoRegisterReportViews(config) { viewName ->
                val deltaTask = createDeltaCoverageViewTask(viewName, config)

                deltaCoverageLifecycleTask.dependsOn(deltaTask)
                deltaCoverageLifecycleTask.summaries.from(deltaTask.summaryReportPath)
                afterEvaluate {
                    if (config.diffSource.git.useNativeGit.get()) {
                        deltaTask.dependsOn(nativeGitDiffTask)
                    }
                }
            }
        }
    }

    private fun Project.autoRegisterReportViews(
        config: DeltaCoverageConfiguration,
        onView: (String) -> Unit,
    ) {
        val registerView = { viewName: String ->
            config.reportViews.maybeCreate(viewName)
            onView(viewName)
        }
        ViewLookup.lookup(this, registerView)
        registerView(DeltaCoverageTaskConfigurer.AGGREGATED_REPORT_VIEW_NAME)
    }

    private fun Project.createDeltaCoverageLifecycle(
        config: DeltaCoverageConfiguration,
    ): DeltaCoverageLifecycleTask {
        return tasks.create(DELTA_COVERAGE_TASK, DeltaCoverageLifecycleTask::class.java) {
            val summaryOutputDir: Provider<String> = config.reportConfiguration.baseReportDir.map { path ->
                project.projectDir.resolveByPath(path).resolve(BASE_COVERAGE_REPORTS_DIR).path
            }
            it.reportDir.set(summaryOutputDir)
        }
    }

    private fun Project.createNativeGitDiffTask(
        config: DeltaCoverageConfiguration,
    ): TaskProvider<NativeGitDiffTask> {
        return tasks.register(GIT_DIFF_TASK, NativeGitDiffTask::class.java) { gitDiffTask ->
            val diffSource = config.diffSource
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
