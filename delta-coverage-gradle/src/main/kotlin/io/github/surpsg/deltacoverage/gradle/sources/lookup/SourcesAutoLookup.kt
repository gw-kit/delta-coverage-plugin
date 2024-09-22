package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import java.nio.file.FileSystems

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
        val viewName: String,
        val deltaCoverageConfiguration: DeltaCoverageConfiguration,
        val objectFactory: ObjectFactory
    )

    companion object {

        fun build(
            coverageEngine: CoverageEngine,
            context: Context,
        ): SourcesAutoLookup = when (coverageEngine) {
            CoverageEngine.JACOCO -> JacocoPluginSourcesLookup(context)
            CoverageEngine.INTELLIJ -> KoverPluginSourcesLookup(FileSystems.getDefault(), context)
        }
    }
}
