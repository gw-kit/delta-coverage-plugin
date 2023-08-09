package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class JacocoPluginSourcesLookupTest {

    private val project: Project = ProjectBuilder.builder().build()

    @ParameterizedTest
    @EnumSource(SourceType::class)
    fun `should return source if source found in jacoco configuration`(
        sourceType: SourceType
    ) {
        // GIVEN
        project.pluginManager.apply {
            apply("java")
            apply("jacoco")
        }

        val autoConfigurator = JacocoPluginSourcesLookup(
            SourcesAutoLookup.Context(
                project,
                DeltaCoverageConfiguration(project.objects),
                project.objects
            )
        )
        // WHEN
        val actualSources: FileCollection = autoConfigurator.lookup(sourceType)

        // THEN
        actualSources shouldHaveAtLeastSize 1
    }

    @ParameterizedTest
    @EnumSource(SourceType::class)
    fun `should return empty source if jacoco not found in project`(
        sourceType: SourceType
    ) {
        // GIVEN
        project.pluginManager.apply("java")

        val autoConfigurator = JacocoPluginSourcesLookup(
            SourcesAutoLookup.Context(
                project,
                DeltaCoverageConfiguration(project.objects),
                project.objects
            )
        )
        // WHEN
        val actualSources: FileCollection = autoConfigurator.lookup(sourceType)

        // THEN
        actualSources shouldHaveSize 0
    }
}
