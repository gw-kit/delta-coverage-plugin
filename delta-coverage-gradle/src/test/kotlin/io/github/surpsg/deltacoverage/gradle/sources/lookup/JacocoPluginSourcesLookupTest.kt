package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.unittest.applyPlugin
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldEndWith
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JacocoPluginSourcesLookupTest {

    private val project: Project = testJavaProject {
        applyPlugin<JacocoPlugin>()
    }

    @ParameterizedTest
    @MethodSource("sourcesParameters")
    fun `should return source if source found in jacoco configuration`(
        sourceType: SourceType,
        expectedFile: String,
    ) {
        // GIVEN
        val sourcesLookup = JacocoPluginSourcesLookup(
            SourcesAutoLookup.Context(
                project = project,
                viewName = JavaPlugin.TEST_TASK_NAME,
                deltaCoverageConfiguration = DeltaCoverageConfiguration(project.objects),
                objectFactory = project.objects,
            )
        )
        // WHEN
        val actualSources: FileCollection = sourcesLookup.lookup(sourceType)

        // THEN
        assertSoftly {
            actualSources shouldHaveAtLeastSize 1
            actualSources.first().absolutePath shouldEndWith expectedFile
        }
    }

    @Test
    fun `should return empty source if jacoco not found in project`() {
        // GIVEN
        val project: Project = testJavaProject()

        val sourcesLookup = JacocoPluginSourcesLookup(
            SourcesAutoLookup.Context(
                project = project,
                viewName = JavaPlugin.TEST_TASK_NAME,
                deltaCoverageConfiguration = DeltaCoverageConfiguration(project.objects),
                objectFactory = project.objects,
            )
        )
        // WHEN
        val actualSources: FileCollection = sourcesLookup.lookup(SourceType.COVERAGE_BINARIES)

        // THEN
        actualSources shouldHaveSize 0
    }

    @Suppress("UnusedPrivateMember")
    private fun sourcesParameters(): List<Arguments> = listOf(
        arguments(SourceType.COVERAGE_BINARIES, "/test.exec"),
        arguments(SourceType.CLASSES, "/build/classes/java/main"),
        arguments(SourceType.SOURCES, "/src/main/java"),
    )
}
