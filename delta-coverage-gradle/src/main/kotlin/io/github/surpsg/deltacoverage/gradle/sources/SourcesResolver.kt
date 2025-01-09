package io.github.surpsg.deltacoverage.gradle.sources

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.filter.SourceFilter
import io.github.surpsg.deltacoverage.gradle.sources.lookup.SourcesAutoLookup
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal object SourcesResolver {

    private val log: Logger = LoggerFactory.getLogger(SourcesResolver::class.java)

    fun resolve(context: Context): FileCollection {
        val sourceType: SourceType = context.sourceType
        val deltaPluginConfiguredSource: FileCollection? = getDeltaPluginConfiguredSource(context)

        val (provider, resolvedSources) = if (deltaPluginConfiguredSource == null) {
            log.debug(
                "{} is not set manually. Attempting to autoconfigure from {} coverage plugin...",
                context.sourceType,
                context.provider.pluginName
            )
            context.provider to context.sourcesAutoLookup.lookup(sourceType)
        } else {
            Provider.DELTA_COVERAGE to deltaPluginConfiguredSource
        }

        return if (resolvedSources.isEmpty) {
            throwMissedConfigurationException(context, provider, sourceType)
        } else {
            log.debug(
                "[{}] {}({}) files were configured from {}",
                context.viewName,
                sourceType.sourceConfigurationPath,
                sourceType.resourceName(provider),
                provider.pluginName
            )
            obtainFilterSource(context, provider, resolvedSources)
        }
    }

    private fun obtainFilterSource(
        context: Context,
        provider: Provider,
        resolvedSources: FileCollection
    ): FileCollection {
        val sourcesToFilter = SourceFilter.InputSource(resolvedSources, provider, context.sourceType)
        return SourceFilter.build(context.viewName, context.config, context.sourceType)
            .filter(sourcesToFilter)
    }

    private fun throwMissedConfigurationException(
        context: Context,
        provider: Provider,
        sourceType: SourceType,
    ): Nothing {
        val errorMessage = if (provider == Provider.DELTA_COVERAGE) {
            "[${context.viewName}] '${sourceType.sourceConfigurationPath}' file collection is empty."
        } else {
            "[${context.viewName}] '${sourceType.sourceConfigurationPath}' is not configured."
        }
        error(errorMessage)
    }

    private fun getDeltaPluginConfiguredSource(
        context: Context,
    ): FileCollection? {
        return when (context.sourceType) {
            SourceType.SOURCES -> context.config.sources
            SourceType.CLASSES -> context.config.classesDirs
            SourceType.COVERAGE_BINARIES -> context.config.reportViews.getAt(context.viewName).coverageBinaryFiles
        }
    }

    class Context private constructor(
        val viewName: String,
        val sourcesAutoLookup: SourcesAutoLookup,
        val sourceType: SourceType,
        private val builder: Builder,
    ) {

        val config: DeltaCoverageConfiguration
            get() = builder.config

        val provider: Provider
            get() = Provider.fromEngine(builder.config.coverage.engine.get())

        class Builder private constructor(
            private val objectFactory: ObjectFactory,
            val project: Project,
            val config: DeltaCoverageConfiguration,
        ) {

            fun build(
                viewName: String,
                sourceType: SourceType
            ): Context {
                val sourceAutoLookup = SourcesAutoLookup.build(
                    coverageEngine = config.coverage.engine.get(),
                    context = SourcesAutoLookup.Context(
                        project = project,
                        viewName = viewName,
                        deltaCoverageConfiguration = config,
                        objectFactory = objectFactory,
                    )
                )
                return Context(viewName, sourceAutoLookup, sourceType, this)
            }

            companion object {

                fun newBuilder(
                    project: Project,
                    objectFactory: ObjectFactory,
                    config: DeltaCoverageConfiguration
                ): Builder = Builder(
                    objectFactory,
                    project,
                    config,
                )
            }
        }
    }

    enum class Provider(val pluginName: String) {
        JACOCO("JaCoCo"),
        KOVER("Kover"),
        DELTA_COVERAGE("Delta-Coverage"),
        ;

        companion object {

            fun fromEngine(engine: CoverageEngine): Provider = when (engine) {
                CoverageEngine.JACOCO -> JACOCO
                CoverageEngine.INTELLIJ -> KOVER
            }
        }
    }
}
