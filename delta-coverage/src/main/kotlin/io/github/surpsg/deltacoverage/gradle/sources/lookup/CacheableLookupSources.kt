package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import org.gradle.api.file.FileCollection

internal abstract class CacheableLookupSources(
    private val lookupContext: SourcesAutoLookup.Context
) : SourcesAutoLookup {

    private val cachedSources: SourcesAutoLookup.AutoDetectedSources by lazy {
        lookupSources(lookupContext)
    }

    final override fun lookup(sourceType: SourceType): FileCollection {
        return when (sourceType) {
            SourceType.CLASSES -> cachedSources.allClasses
            SourceType.SOURCES -> cachedSources.allSources
            SourceType.COVERAGE_BINARIES -> cachedSources.allBinaryCoverageFiles
        }
    }

    abstract fun lookupSources(lookupContext: SourcesAutoLookup.Context): SourcesAutoLookup.AutoDetectedSources
}
