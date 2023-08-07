package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory

internal interface SourcesAutoLookup {

    /**
     * Lookups sources by type.
     *
     * @param sourceType type of source to lookup.
     * @return file collection of found sources.
     */
    fun lookup(sourceType: SourceType): FileCollection

    data class Context(
        val project: Project,
        val deltaCoverageConfiguration: DeltaCoverageConfiguration,
        val objectFactory: ObjectFactory
    )

    data class AutoDetectedSources(
        val allExecFiles: ConfigurableFileCollection,
        val allClasses: ConfigurableFileCollection,
        val allSources: ConfigurableFileCollection
    )

    companion object {

        fun build(
            coverageEngine: CoverageEngine,
            context: Context,
        ): SourcesAutoLookup = when (coverageEngine) {
            CoverageEngine.JACOCO -> JacocoPluginSourcesLookup(context)
            CoverageEngine.INTELLIJ -> TODO()
        }

        fun ObjectFactory.newAutoDetectedSources() = AutoDetectedSources(
            fileCollection(),
            fileCollection(),
            fileCollection()
        )
    }

}
