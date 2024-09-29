package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.autoapply.CoverageEngineAutoApply
import io.github.surpsg.deltacoverage.gradle.reportview.ViewLookup
import io.github.surpsg.deltacoverage.gradle.sources.lookup.KoverPluginSourcesLookup
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTask
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTaskConfigurer
import io.github.surpsg.deltacoverage.gradle.task.NativeGitDiffTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class DeltaCoveragePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val deltaCoverageConfig: DeltaCoverageConfiguration = project.extensions.create(
            DELTA_COVERAGE_REPORT_EXTENSION,
            DeltaCoverageConfiguration::class.java,
            project.objects,
        )
        project.autoRegisterReportViews()

        CoverageEngineAutoApply().apply(project, deltaCoverageConfig)

        val deltaCoverageTask: DeltaCoverageTask = project.tasks.create(
            DELTA_COVERAGE_TASK,
            DeltaCoverageTask::class.java
        ) { deltaCoverageTask ->
            DeltaCoverageTaskConfigurer.configure(deltaCoverageConfig, deltaCoverageTask)
        }

        project.tasks.register(GIT_DIFF_TASK, NativeGitDiffTask::class.java) { gitDiffTask ->
            val diffSource = deltaCoverageConfig.diffSource
            if (diffSource.git.useNativeGit.get()) {
                gitDiffTask.targetBranch.set(diffSource.git.diffBase)

                diffSource.git.nativeGitDiffFile.set(gitDiffTask.diffFile)

                deltaCoverageTask.dependsOn(gitDiffTask)
            }
            gitDiffTask.dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        }
    }

    private fun Project.autoRegisterReportViews() =
        extensions.configure(DeltaCoverageConfiguration::class.java) { config ->
            ViewLookup.lookup(this, config.reportViews::maybeCreate)
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
