package io.github.surpsg.deltacoverage.gradle.autoapply

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.autoapply.CoverageEngineAutoApply.Companion.JACOCO_PLUGIN_ID
import io.github.surpsg.deltacoverage.gradle.autoapply.CoverageEngineAutoApply.Companion.KOVER_PLUGIN_ID
import io.github.surpsg.deltacoverage.gradle.unittest.applyDeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import org.gradle.api.internal.project.ProjectInternal
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CoverageEngineAutoApplyTest {

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
        val project: ProjectInternal = testJavaProject(attachSettings = true) {
            applyDeltaCoveragePlugin()

            extensions.configure(DeltaCoverageConfiguration::class.java) {
                it.coverage.engine.set(coverageEngine)
                it.coverage.autoApplyPlugin.set(expectedIsApplied)
            }
        }

        // WHEN
        val hasPlugin = project.plugins.hasPlugin(coveragePluginId)

        // THEN
        hasPlugin shouldBeEqualComparingTo expectedIsApplied
    }
}
