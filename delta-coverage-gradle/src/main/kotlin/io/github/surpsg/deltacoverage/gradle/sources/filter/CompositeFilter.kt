package io.github.surpsg.deltacoverage.gradle.sources.filter

import org.gradle.api.file.FileCollection

internal class CompositeFilter(
    private val filters: List<SourceFilter>
) : SourceFilter {
    override fun filter(
        inputSource: SourceFilter.InputSource
    ): FileCollection {
        return filters.fold(inputSource.originSources) { filteredSources, filter ->
            val sources = SourceFilter.InputSource(filteredSources, inputSource.provider, inputSource.sourceType)
            filter.filter(sources)
        }
    }
}
