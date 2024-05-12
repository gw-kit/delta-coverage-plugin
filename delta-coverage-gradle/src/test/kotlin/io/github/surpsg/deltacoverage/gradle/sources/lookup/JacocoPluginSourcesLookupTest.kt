package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class JacocoPluginSourcesLookupTest {

    @ParameterizedTest
    @EnumSource(SourceType::class)
    fun `should return source if source found in jacoco configuration`(
        sourceType: SourceType
    ) {
        // GIVEN
        val project: Project = testJavaProject {
            pluginManager.apply("jacoco")
        }

        val sourcesLookup = JacocoPluginSourcesLookup(
            SourcesAutoLookup.Context(
                project,
                DeltaCoverageConfiguration(project.objects),
                project.objects
            )
        )
        // WHEN
        val actualSources: FileCollection = sourcesLookup.lookup(sourceType)

        // THEN
        actualSources shouldHaveAtLeastSize 1
    }

    @Test
    fun `should return empty source if jacoco not found in project`() {
        // GIVEN
        val project: Project = testJavaProject()

        val sourcesLookup = JacocoPluginSourcesLookup(
            SourcesAutoLookup.Context(
                project,
                DeltaCoverageConfiguration(project.objects),
                project.objects
            )
        )
        // WHEN
        val actualSources: FileCollection = sourcesLookup.lookup(SourceType.COVERAGE_BINARIES)

        // THEN
        actualSources shouldHaveSize 0
    }
}
