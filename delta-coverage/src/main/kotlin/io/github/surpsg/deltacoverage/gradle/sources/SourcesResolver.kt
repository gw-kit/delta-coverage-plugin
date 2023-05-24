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

internal class SourcesResolver {

    fun resolve(context: Context): FileCollection {
        val sourceType: SourceType = context.sourceType
        val deltaPluginConfiguredSource: FileCollection? = getDeltaPluginConfiguredSource(
            context.sourceType,
            context.config
        )

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
            throwMissedConfigurationException(provider, sourceType)
        } else {
            log.debug(
                "{}({}) files were configured from {}",
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
        return SourceFilter.build(context.config, context.sourceType)
            .filter(sourcesToFilter)
    }

    private fun throwMissedConfigurationException(
        provider: Provider,
        sourceType: SourceType
    ): Nothing {
        val errorMessage = if (provider == Provider.DELTA_COVERAGE) {
            "'${sourceType.sourceConfigurationPath}' file collection is empty."
        } else {
            "'${sourceType.sourceConfigurationPath}' is not configured."
        }
        throw IllegalArgumentException(errorMessage)
    }

    private fun getDeltaPluginConfiguredSource(
        sourceType: SourceType,
        deltaCoverageConfiguration: DeltaCoverageConfiguration
    ): FileCollection? {
        return when (sourceType) {
            SourceType.CLASSES -> deltaCoverageConfiguration.classesDirs
            SourceType.SOURCES -> deltaCoverageConfiguration.srcDirs
            SourceType.COVERAGE_BINARIES -> deltaCoverageConfiguration.coverageBinaryFiles
        }
    }

    class Context private constructor(
        val sourcesAutoLookup: SourcesAutoLookup,
        val sourceType: SourceType,
        private val builder: Builder,
    ) {

        val config: DeltaCoverageConfiguration
            get() = builder.config

        val provider: Provider
            get() = Provider.fromEngine(builder.config.coverageEngine)

        class Builder private constructor(
            private val objectFactory: ObjectFactory,
            val project: Project,
            val config: DeltaCoverageConfiguration,
        ) {

            fun build(
                sourceType: SourceType
            ): Context {
                val sourceAutoLookup = SourcesAutoLookup.build(
                    config.coverageEngine,
                    SourcesAutoLookup.Context(project, config, objectFactory)
                )
                return Context(sourceAutoLookup, sourceType, this)
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
            }
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(SourcesResolver::class.java)
    }
}
