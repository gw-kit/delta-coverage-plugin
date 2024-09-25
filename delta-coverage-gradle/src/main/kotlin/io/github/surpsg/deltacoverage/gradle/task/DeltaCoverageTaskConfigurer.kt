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

private typealias CollectionProvider<T> = Collection<Provider<T>>
private typealias MapProvider<K, V> = Map<K, Provider<V>>

internal object DeltaCoverageTaskConfigurer {

    const val AGGREGATED_REPORT_VIEW_NAME = "aggregated"

    fun configure(
        deltaCoverageConfig: DeltaCoverageConfiguration,
        deltaCoverageTask: DeltaCoverageTask,
    ) = with(deltaCoverageTask) {
        deltaCoverageConfigProperty.set(deltaCoverageConfig)
        configureDependencies()
        applySourcesInputs(deltaCoverageConfig)
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
        config: DeltaCoverageConfiguration
    ): Any = project.gradle.taskGraph.whenReady {
        project.obtainViewSources(config)
            .forEach { (view, viewSourcesProvider) ->
                sourcesFiles.set(viewSourcesProvider.map { it.sources })
                classesFiles.set(viewSourcesProvider.map { it.classes })
                coverageBinaryFiles.put(view, viewSourcesProvider.map { it.coverageBinaries })
            }
    }

    private fun Project.obtainViewSources(
        config: DeltaCoverageConfiguration,
    ): Map<String, Provider<ViewSources>> {
        val contextBuilder = SourcesResolver.Context.Builder.newBuilder(project, project.objects, config)
        val viewSources: MapProvider<String, ViewSources> = config.reportViews
            .asSequence()
            .filter { it.name != AGGREGATED_REPORT_VIEW_NAME }
            .map { view ->
                view.name to provider {
                    ViewSources(
                        viewName = view.name,
                        sources = resolveSource(view.name, contextBuilder, SourceType.SOURCES),
                        classes = resolveSource(view.name, contextBuilder, SourceType.CLASSES),
                        coverageBinaries = resolveSource(view.name, contextBuilder, SourceType.COVERAGE_BINARIES)
                    )
                }
            }
            .toMap()

        val aggregated: Provider<ViewSources> = project.buildAggregatedViewSources(viewSources.values)

        return viewSources + (AGGREGATED_REPORT_VIEW_NAME to aggregated)
    }

    private fun Project.buildAggregatedViewSources(
        viewSources: CollectionProvider<ViewSources>,
    ): Provider<ViewSources> {
        val seedProvider = provider {
            ViewSources(
                viewName = AGGREGATED_REPORT_VIEW_NAME,
                sources = files(),
                classes = files(),
                coverageBinaries = files(),
            )
        }
        return viewSources.fold(seedProvider) { accProvider, nextSourcesProvider ->
            accProvider.flatMap { acc ->
                nextSourcesProvider.map { next ->
                    ViewSources(
                        viewName = acc.viewName,
                        sources = acc.sources + next.sources,
                        classes = acc.classes + next.classes,
                        coverageBinaries = acc.coverageBinaries + next.coverageBinaries
                    )
                }
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
    )
}
