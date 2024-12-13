package io.github.surpsg.deltacoverage.gradle.sources.filter

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.sources.SourcesResolver
import org.gradle.api.file.FileCollection

internal fun interface SourceFilter {

    fun filter(inputSource: InputSource): FileCollection

    companion object {

        private val NOOP_FILTER: SourceFilter = SourceFilter { it.originSources }

        fun build(
            view: String,
            config: DeltaCoverageConfiguration,
            sourceType: SourceType
        ): SourceFilter {
            return when (sourceType) {
                SourceType.CLASSES -> buildClassesFilter(view, config)
                else -> NOOP_FILTER
            }
        }

        private fun buildClassesFilter(
            viewName: String,
            config: DeltaCoverageConfiguration,
        ): SourceFilter {
            val includePatterns: List<String> = config.reportViews.findByName(viewName)?.matchClasses
                ?.orNull ?: emptyList()

            val includeFilter: SourceFilter = if (includePatterns.isEmpty()) {
                NOOP_FILTER
            } else {
                AntSourceIncludeFilter(includePatterns)
            }
            return CompositeFilter(
                listOf(
                    AntSourceExcludeFilter(config.excludeClasses.get()),
                    includeFilter,
                )
            )
        }
    }

    data class InputSource(
        val originSources: FileCollection,
        val provider: SourcesResolver.Provider,
        val sourceType: SourceType
    )
}
