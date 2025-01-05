package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_TASK_DEPENDENCIES
import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.log
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.sources.SourcesResolver
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test

internal object DeltaCoverageTaskConfigurer {

    const val AGGREGATED_REPORT_VIEW_NAME = "aggregated"

    fun configure(
        view: String,
        deltaCoverageConfig: DeltaCoverageConfiguration,
        deltaCoverageTask: DeltaCoverageTask,
    ) = with(deltaCoverageTask) {
        viewName.set(view)
        deltaCoverageConfigProperty.set(deltaCoverageConfig)
        configureDependencies()
        applySourcesInputs(view, deltaCoverageConfig)
    }

    private fun DeltaCoverageTask.configureDependencies() = project.afterEvaluate {
        val deltaCoverageTask: DeltaCoverageTask = this
        project.getAllTasks(true).values.asSequence()
            .flatMap { it.asSequence() }
            .forEach { task ->
                deltaCoverageTask.configureDependencyOn(task)
            }
    }

    private fun DeltaCoverageTask.configureDependencyOn(task: Task) = when {
        task.name in DELTA_TASK_DEPENDENCIES -> {
            log.info("Configuring {} to depend on {}", this, task)
            dependsOn(task)
        }

        task is Test -> {
            log.info("Configuring {} to run after {}", this, task)
            mustRunAfter(task)
        }

        else -> this
    }

    private fun DeltaCoverageTask.applySourcesInputs(
        viewName: String,
        config: DeltaCoverageConfiguration
    ) = project.gradle.taskGraph.whenReady {
        val viewSourcesProvider: Provider<ViewSources> = project.obtainViewSources(viewName, config)
        sourcesFiles.set(
            viewSourcesProvider.map { it.sources }
        )
        classesFiles.set(
            viewSourcesProvider.map { it.classes }
        )
        coverageBinaryFiles.set(
            viewSourcesProvider.map { it.coverageBinaries }
        )
    }

    private fun Project.obtainViewSources(
        viewName: String,
        config: DeltaCoverageConfiguration,
    ): Provider<ViewSources> {
        val contextBuilder = SourcesResolver.Context.Builder.newBuilder(project, project.objects, config)
        return if (viewName == AGGREGATED_REPORT_VIEW_NAME) {
            project.buildAggregatedViewSources(config, contextBuilder)
        } else {
            provider {
                buildViewSources(viewName, contextBuilder)
            }
        }
    }

    private fun buildViewSources(
        viewName: String,
        contextBuilder: SourcesResolver.Context.Builder,
    ): ViewSources = ViewSources(
        viewName = viewName,
        sources = resolveSource(viewName, contextBuilder, SourceType.SOURCES),
        classes = resolveSource(viewName, contextBuilder, SourceType.CLASSES),
        coverageBinaries = resolveSource(viewName, contextBuilder, SourceType.COVERAGE_BINARIES)
    )

    private fun Project.buildAggregatedViewSources(
        config: DeltaCoverageConfiguration,
        contextBuilder: SourcesResolver.Context.Builder,
    ): Provider<ViewSources> {
        val seedProvider = provider {
            ViewSources(
                viewName = AGGREGATED_REPORT_VIEW_NAME,
                sources = files(),
                classes = files(),
                coverageBinaries = files(),
            )
        }
        return config.reportViews
            .asSequence()
            .filter { it.name != AGGREGATED_REPORT_VIEW_NAME }
            .fold(seedProvider) { accProvider, view ->
                accProvider.map { acc ->
                    acc + buildViewSources(view.name, contextBuilder)
                }
            }
    }

    private fun resolveSource(
        viewName: String,
        contextBuilder: SourcesResolver.Context.Builder,
        sourceType: SourceType,
    ): FileCollection {
        val resolveContext: SourcesResolver.Context = contextBuilder.build(viewName, sourceType)
        return SourcesResolver.resolve(resolveContext)
    }

    private data class ViewSources(
        val viewName: String,
        val sources: FileCollection,
        val classes: FileCollection,
        val coverageBinaries: FileCollection,
    ) {

        operator fun plus(other: ViewSources): ViewSources = ViewSources(
            viewName = viewName,
            sources = sources + other.sources,
            classes = classes + other.classes,
            coverageBinaries = coverageBinaries + other.coverageBinaries,
        )
    }
}
