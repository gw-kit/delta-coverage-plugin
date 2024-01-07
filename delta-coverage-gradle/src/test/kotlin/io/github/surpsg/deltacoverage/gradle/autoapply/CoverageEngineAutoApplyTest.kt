package io.github.surpsg.deltacoverage.gradle.autoapply

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.autoapply.CoverageEngineAutoApply.Companion.JACOCO_PLUGIN_ID
import io.github.surpsg.deltacoverage.gradle.autoapply.CoverageEngineAutoApply.Companion.KOVER_PLUGIN_ID
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CoverageEngineAutoApplyTest {

    private val project: ProjectInternal = buildTestProject()

    @ParameterizedTest
    @CsvSource(
        "INTELLIJ, $KOVER_PLUGIN_ID, true",
        "INTELLIJ, $KOVER_PLUGIN_ID, false",

        "JACOCO, $JACOCO_PLUGIN_ID, true",
        "JACOCO, $JACOCO_PLUGIN_ID, false",
    )
    fun `should apply coverage plugin if auto-apply is set to true`(
        coverageEngine: CoverageEngine,
        coveragePluginId: String,
        expectedIsApplied: Boolean,
    ) {
        // GIVEN
        project.configureProjectWithDeltaCoverage {
            coverage.engine.set(coverageEngine)
            coverage.autoApplyPlugin.set(expectedIsApplied)
        }

        // WHEN
        project.evaluate()

        // THEN
        project.plugins.hasPlugin(coveragePluginId) shouldBeEqualComparingTo expectedIsApplied
    }

    private fun buildTestProject(): ProjectInternal {
        val project = ProjectBuilder.builder().build() as ProjectInternal
        project.gradle.attachSettings(
            mockk(relaxed = true)
        )
        return project
    }

    private fun Project.configureProjectWithDeltaCoverage(
        applyConfiguration: DeltaCoverageConfiguration.() -> Unit,
    ) = with(project) {
        pluginManager.apply("java")
        pluginManager.apply("io.github.surpsg.delta-coverage")

        project.extensions.configure(DeltaCoverageConfiguration::class.java) {
            it.applyConfiguration()
        }
    }
}
