package io.github.surpsg.deltacoverage.gradle.sources.lookup

import io.github.gwkit.coverjet.gradle.CoverJetPlugin
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.sources.SourceType
import io.github.surpsg.deltacoverage.gradle.unittest.applyKotlinPlugin
import io.github.surpsg.deltacoverage.gradle.unittest.applyPlugin
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CoverJetPluginSourcesLookupTest {

    @Test
    fun `should return empty source if cover-jet plugin is not applied`() {
        // GIVEN
        val project: Project = testJavaProject()

        val coverJetPluginSourcesLookup = CoverJetPluginSourcesLookup(
            SourcesAutoLookup.Context(
                project = project,
                viewName = JavaPlugin.TEST_TASK_NAME,
                deltaCoverageConfiguration = DeltaCoverageConfiguration(project.objects),
                objectFactory = project.objects,
            )
        )

        // WHEN
        val actualSources: FileCollection = coverJetPluginSourcesLookup.lookup(SourceType.COVERAGE_BINARIES)

        // THEN
        actualSources shouldHaveSize 0
    }

    @ParameterizedTest
    @EnumSource(SourceType::class)
    fun `should return non empty files if source found in cover jet configuration`(
        sourceType: SourceType
    ) {
        // GIVEN
        val project: Project = testJavaProject {
            applyKotlinPlugin()
            applyPlugin<CoverJetPlugin>()
        }

        val coverJetPluginSourcesLookup = CoverJetPluginSourcesLookup(
            SourcesAutoLookup.Context(
                project = project,
                viewName = JavaPlugin.TEST_TASK_NAME,
                deltaCoverageConfiguration = DeltaCoverageConfiguration(project.objects),
                objectFactory = project.objects,
            )
        )

        // WHEN
        val actualSources: FileCollection = coverJetPluginSourcesLookup.lookup(sourceType)

        // THEN
        assertSoftly(actualSources) {
            shouldHaveAtLeastSize(1)
        }
    }
}
