package io.github.surpsg.deltacoverage.gradle.sources.filter

import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.sources.SourcesResolver
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSingleElement
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class AntSourceExcludeFilterTest {

    private val project: Project = testJavaProject()

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

    @ParameterizedTest
    @CsvSource(
        "exclude-1.txt, **/exclude-1.txt",
        "exclude-2.txt, **/exclude-*.txt",
        "a/exclude-3.txt, **/exclude-*.txt",
        "a/exclude-4.txt, **/a/*",
        "a/b/exclude-5.txt, **/a/b/**",
        "a/b/exclude-6.txt, **/b/**",
        "a/b/c/d/exclude-7.txt, **/a/**/d/**",
    )
    fun `should filter exclude file if matched to filter`(
        filePathToExclude: String,
        excludePattern: String,
    ) {
        // GIVEN
        val expectedFile = "file-to-keep.txt"
        val excludeFilters = AntSourceExcludeFilter(listOf(excludePattern))
        val originSources = project.files(
            project.layout.buildDirectory.file(filePathToExclude).get().asFile,
            expectedFile
        ).onEach {
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
