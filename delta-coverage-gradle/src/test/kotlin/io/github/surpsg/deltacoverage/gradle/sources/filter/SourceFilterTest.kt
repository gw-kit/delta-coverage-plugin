package io.github.surpsg.deltacoverage.gradle.sources.filter

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.sources.SourcesResolver
import io.github.surpsg.deltacoverage.gradle.unittest.applyDeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSingleElement
import org.gradle.api.file.FileCollection
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class SourceFilterTest {

    @Test
    fun `should build composite filter with include filter`() {
        // GIVEN
        val includePattern = "**/some/example/*ShouldBeKept*"
        val fileToInclude = "some/example/ShouldBeKept.class"
        val fileToExclude = "some/another/ExpectedToBeExcluded.class"

        val viewName = "sourceFilterBuildTest"
        val proj = testJavaProject {
            applyDeltaCoveragePlugin()
            extensions.configure(DeltaCoverageConfiguration::class.java) { config ->
                config.view(viewName) {
                    it.matchClasses.add(includePattern)
                }
            }
        }

        val expectedFile: File = proj.layout.buildDirectory.file(fileToInclude).get().asFile
        val originSources = proj.files(
            fileToExclude,
            expectedFile
        ).onEach {
            it.parentFile.mkdirs()
            it.createNewFile()
        }

        val actualFilters: SourceFilter = SourceFilter.build(
            viewName,
            proj.extensions.getByType(DeltaCoverageConfiguration::class.java),
            SourceType.CLASSES
        )

        // WHEN
        val actualFilteredFiles: FileCollection = actualFilters.filter(
            SourceFilter.InputSource(
                originSources,
                SourcesResolver.Provider.DELTA_COVERAGE,
                SourceType.CLASSES
            )
        )

        // THEN
        assertSoftly(actualFilteredFiles) {
            map { it.name } shouldHaveSingleElement expectedFile.name
        }
    }

    @ParameterizedTest
    @MethodSource("noOpFilterTest")
    fun `should build no op filter`(
        viewName: String,
        includePatterns: List<String>,
    ) {
        // GIVEN
        val expectedFiles = listOf(
            "ShouldBeKept1.class",
            "ShouldBeKept2.class"
        )
        val proj = testJavaProject {
            applyDeltaCoveragePlugin()
            extensions.configure(DeltaCoverageConfiguration::class.java) { config ->
                config.view("noOpFilterTest") {
                    it.matchClasses.addAll(includePatterns)
                }
            }
        }

        val originSources = proj.files(expectedFiles).onEach {
            it.parentFile.mkdirs()
            it.createNewFile()
        }

        val actualFilters: SourceFilter = SourceFilter.build(
            viewName,
            proj.extensions.getByType(DeltaCoverageConfiguration::class.java),
            SourceType.CLASSES
        )

        // WHEN
        val actualFilteredFiles: FileCollection = actualFilters.filter(
            SourceFilter.InputSource(
                originSources,
                SourcesResolver.Provider.DELTA_COVERAGE,
                SourceType.CLASSES
            )
        )

        // THEN
        assertSoftly(actualFilteredFiles) {
            map { it.name } shouldContainExactlyInAnyOrder expectedFiles
        }
    }

    private companion object {
        @JvmStatic
        fun noOpFilterTest() = listOf(
            arguments("noOpFilterTest", listOf<String>()),
            arguments("noOpFilterTest", listOf("")),

            arguments("unknownView", listOf("**/UnknownFiles*")),
            arguments("unknownView", listOf<String>()),
            arguments("unknownView", listOf("")),
        )
    }
}
