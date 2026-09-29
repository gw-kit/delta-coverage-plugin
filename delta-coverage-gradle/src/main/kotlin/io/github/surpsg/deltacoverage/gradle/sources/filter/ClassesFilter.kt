package io.github.surpsg.deltacoverage.gradle.sources.filter

import org.gradle.api.file.FileCollection

internal fun interface ClassesFilter {

    fun filter(files: FileCollection): FileCollection

    companion object {

        fun build(
            filterConfigurator: FilterBuilder.() -> Unit,
        ): ClassesFilter = FilterBuilder().apply(filterConfigurator).build()
    }

    class FilterBuilder {

        private val NOOP_FILTER: ClassesFilter = ClassesFilter { it }

        private val includeFilters: MutableSet<String> = mutableSetOf()

        private val excludeFilters: MutableSet<String> = mutableSetOf()

        fun include(patterns: Iterable<String>) {
            includeFilters.addAll(patterns)
        }

        fun exclude(patterns: Iterable<String>) {
            excludeFilters.addAll(patterns)
        }

        internal fun build(): ClassesFilter = listOfNotNull(
            includeFilters.nonEmptyOrNull()?.let(::AntSourceIncludeFilter) ?: NOOP_FILTER,
            excludeFilters.nonEmptyOrNull()?.let(::AntSourceExcludeFilter),
        ).let(::CompositeFilter)

        private fun Iterable<String>.nonEmptyOrNull() =
            filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }

    }

    /**
     * A composite filter that applies multiple filters in sequence.
     */
    private class CompositeFilter(
        private val filters: Iterable<ClassesFilter>,
    ) : ClassesFilter {

        override fun filter(files: FileCollection): FileCollection {
            return filters.fold(files) { acc, filter ->
                filter.filter(acc)
            }
        }
    }
}
