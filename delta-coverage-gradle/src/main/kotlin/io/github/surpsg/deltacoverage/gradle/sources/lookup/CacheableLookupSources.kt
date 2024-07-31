package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import org.gradle.api.file.FileCollection

internal abstract class CacheableLookupSources(
    private val lookupContext: SourcesAutoLookup.Context
) : SourcesAutoLookup {

    private val cachedCoverageBinaries: FileCollection by lazy {
        lookupCoverageBinaries(lookupContext)
    }

    private val cachedSourceSets: SourceSetsLookup.AutoDetectedSources by lazy {
        SourceSetsLookup().lookupSourceSets(lookupContext.project)
    }

    final override fun lookup(sourceType: SourceType): FileCollection {
        return when (sourceType) {
            SourceType.CLASSES -> cachedSourceSets.allClasses
            SourceType.SOURCES -> cachedSourceSets.allSources
            SourceType.COVERAGE_BINARIES -> cachedCoverageBinaries
        }
    }

    abstract fun lookupCoverageBinaries(lookupContext: SourcesAutoLookup.Context): FileCollection
}
