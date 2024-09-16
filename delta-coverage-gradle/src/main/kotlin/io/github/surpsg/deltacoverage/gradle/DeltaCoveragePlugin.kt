package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.autoapply.CoverageEngineAutoApply
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.sources.SourcesResolver
import io.github.surpsg.deltacoverage.gradle.sources.lookup.KoverPluginSourcesLookup
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

open class DeltaCoveragePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val deltaCoverageConfig: DeltaCoverageConfiguration = project.extensions.create(
            DELTA_COVERAGE_REPORT_EXTENSION,
            DeltaCoverageConfiguration::class.java,
            project,
            project.objects,
        )
        project.configureReportViews()

        CoverageEngineAutoApply().apply(project, deltaCoverageConfig)

        val deltaCoverageTask: DeltaCoverageTask = project.tasks.create(
            DELTA_COVERAGE_TASK,
            DeltaCoverageTask::class.java
        ) { deltaCoverageTask ->
            with(deltaCoverageTask) {
                configureDependencies()
                deltaCoverageConfigProperty.set(deltaCoverageConfig)
                applySourcesInputs(deltaCoverageConfig)
            }
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

    private fun Project.configureReportViews() {
        val evaluatedTestTasks: MutableSet<String> = ConcurrentHashMap.newKeySet()
        project.extensions.configure(DeltaCoverageConfiguration::class.java) { config ->
            project.allprojects { proj ->
                proj.tasks.withType(Test::class.java).configureEach { testTask ->
                    val newItemAdded: Boolean = evaluatedTestTasks.add(testTask.name)
                    if (newItemAdded) {
                        config.reportViews.maybeCreate(testTask.name)
                    }
                }
            }
        }
    }

    private fun DeltaCoverageTask.configureDependencies() = project.afterEvaluate {
        val deltaCoverageTask: DeltaCoverageTask = this
        project.getAllTasks(true).values.asSequence()
            .flatMap { it.asSequence() }
            .forEach { task ->
                configureDependencyFromTask(deltaCoverageTask, task)
            }
    }

    private fun configureDependencyFromTask(deltaCoverageTask: DeltaCoverageTask, task: Task) {
        if (task.name in DELTA_TASK_DEPENDENCIES) {
            log.info("Configuring {} to depend on {}", deltaCoverageTask, task)
            deltaCoverageTask.dependsOn(task)
        }

        if (task is Test) {
            log.info("Configuring {} to run after {}", deltaCoverageTask, task)
            deltaCoverageTask.mustRunAfter(task)
        }
    }

    private fun DeltaCoverageTask.applySourcesInputs(
        config: DeltaCoverageConfiguration
    ) = project.gradle.taskGraph.whenReady {
        config.reportViews.forEach { view ->
            applySourcesInputs(view.name, config)
        }
    }

    private fun DeltaCoverageTask.applySourcesInputs(
        viewName: String,
        config: DeltaCoverageConfiguration
    ) {
        val contextBuilder = SourcesResolver.Context.Builder.newBuilder(project, project.objects, config)

        val sourcesResolver = SourcesResolver()
        sequenceOf(
            classesFiles to SourceType.CLASSES,
            sourcesFiles to SourceType.SOURCES,
        ).forEach { (taskSourceProperty, sourceType) ->
            taskSourceProperty.value(
                project.provider {
                    val resolveContext: SourcesResolver.Context = contextBuilder.build(viewName, sourceType)
                    sourcesResolver.resolve(resolveContext)
                }
            )
        }

        coverageBinaryFiles.put(
            viewName,
            project.provider {
                val resolveContext: SourcesResolver.Context = contextBuilder.build(
                    viewName,
                    SourceType.COVERAGE_BINARIES,
                )
                sourcesResolver.resolve(resolveContext)
            }
        )
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
