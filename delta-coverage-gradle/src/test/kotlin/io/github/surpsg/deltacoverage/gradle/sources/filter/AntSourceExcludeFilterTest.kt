package io.github.surpsg.deltacoverage.gradle.sources.filter

import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.sources.SourcesResolver
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSingleElement
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

internal class AntSourceExcludeFilterTest {

    private val project: Project = ProjectBuilder.builder().build()

    @Test
    fun `should return origin sources if filters collection is empty`() {
        // GIVEN
        val expectedFiles = arrayOf("file.1", "file.2")

        val emptyFilters = AntSourceExcludeFilter(emptyList())

        val inputSource = SourceFilter.InputSource(
            project.files(*expectedFiles),
            SourcesResolver.Provider.DELTA_COVERAGE,
            SourceType.SOURCES
        )

        // WHEN
        val actualFilteredFiles: FileCollection = emptyFilters.filter(inputSource)

        // THEN
        assertSoftly(actualFilteredFiles) {
            map { it.name }.shouldContainExactlyInAnyOrder(*expectedFiles)
        }
    }

    @Test
    fun `should filter exclude file if matched to filter`() {
        // GIVEN
        val expectedFile = "file-2.txt"
        val excludeFilters = AntSourceExcludeFilter(listOf("file-1.txt"))
        val originSources = project.files("file-1.txt", expectedFile).onEach {
            it.parentFile.mkdirs()
            it.createNewFile()
        }

        val inputSource = SourceFilter.InputSource(
            originSources,
            SourcesResolver.Provider.DELTA_COVERAGE,
            SourceType.SOURCES
        )

        // WHEN
        val actualFilteredFiles: FileCollection = excludeFilters.filter(inputSource)

        // THEN
        assertSoftly(actualFilteredFiles) {
            map { it.name } shouldHaveSingleElement expectedFile
        }
    }
}
