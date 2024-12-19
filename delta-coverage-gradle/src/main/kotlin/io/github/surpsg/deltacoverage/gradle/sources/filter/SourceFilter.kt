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
            val includeFilter: SourceFilter = if (config.reportViews.names.contains(viewName)) {
                config.reportViews.getByName(viewName).matchClasses.get()
                    .filter { it.isNotBlank() }
                    .takeIf { it.isNotEmpty() }
                    ?.let { AntSourceIncludeFilter(it) }
                    ?: NOOP_FILTER
            } else {
                NOOP_FILTER
            }
            return if (includeFilter == NOOP_FILTER) {
                AntSourceExcludeFilter(config.excludeClasses.get())
            } else {
                includeFilter
            }
        }
    }

    data class InputSource(
        val originSources: FileCollection,
        val provider: SourcesResolver.Provider,
        val sourceType: SourceType
    )
}
