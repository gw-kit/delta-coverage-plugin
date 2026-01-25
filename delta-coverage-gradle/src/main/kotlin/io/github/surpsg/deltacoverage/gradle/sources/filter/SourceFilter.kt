package io.github.surpsg.deltacoverage.gradle.sources.filter

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.ReportView
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
        ) = CompositeFilter().apply {
            val view: ReportView = config.reportViews.getByName(viewName)

            // Get include patterns from view
            view.includeClasses.get()
                .nonEmptyOrNull()
                ?.let(::AntSourceIncludeFilter)
                ?.let(::addFilter)

            // Get exclude patterns: global + view-level
            (config.excludeClasses.get() + view.excludeClasses.get())
                .nonEmptyOrNull()
                ?.let(::AntSourceExcludeFilter)
                ?.let(::addFilter)
        }

        private fun List<String>.nonEmptyOrNull() =
            filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
    }

    data class InputSource(
        val originSources: FileCollection,
        val provider: SourcesResolver.Provider,
        val sourceType: SourceType
    )

    /**
     * A composite filter that applies multiple filters in sequence.
     */
    private class CompositeFilter : SourceFilter {

        private val filters: MutableList<SourceFilter> = mutableListOf(NOOP_FILTER)

        override fun filter(inputSource: InputSource): FileCollection =
            filters.fold(inputSource.originSources) { acc, filter ->
                filter.filter(inputSource.copy(originSources = acc))
            }

        fun addFilter(filter: SourceFilter): CompositeFilter {
            filters += filter
            return this
        }
    }
}
