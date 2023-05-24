package io.github.surpsg.deltacoverage.gradle.sources.filter

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.sources.SourcesResolver
import org.gradle.api.file.FileCollection

internal fun interface SourceFilter {

    fun filter(inputSource: InputSource): FileCollection

    companion object {

        private val NOOP_FILTER: SourceFilter = SourceFilter { it.originSourcesToFilter }

        fun build(
            config: DeltaCoverageConfiguration,
            sourceType: SourceType
        ): SourceFilter {
            return when (sourceType) {
                SourceType.CLASSES -> AntSourceExcludeFilter(config.excludeClasses.get())
                else -> NOOP_FILTER
            }
        }
    }

    data class InputSource(
        val originSourcesToFilter: FileCollection,
        val provider: SourcesResolver.Provider,
        val sourceType: SourceType
    )
}
