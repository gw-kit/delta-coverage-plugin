package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.unittest.applyDeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.api.internal.project.ProjectInternal
import org.junit.jupiter.api.Test

class DeltaCoverageConfigurationTest {

    @Test
    fun `view should add reportView`() {
        // GIVEN
        val project: ProjectInternal = testJavaProject {
            applyDeltaCoveragePlugin()
        }
        val config: DeltaCoverageConfiguration = project.extensions.getByType(DeltaCoverageConfiguration::class.java)
        val customView = "customView"

        // WHEN
        config.view(customView) {}

        // THEN
        config.reportViews.getByName(customView).shouldNotBeNull()
    }

    @Test
    fun `view should configure existing reportView`() {
        // GIVEN
        val expectedMinCoverage = 1.0
        val customView = "customView"

        val project: ProjectInternal = testJavaProject {
            applyDeltaCoveragePlugin()
            extensions.configure(DeltaCoverageConfiguration::class.java) { config ->
                config.view(customView) { view ->
                    view.violationRules.failIfCoverageLessThan(0.0)
                }
            }
        }
        val config: DeltaCoverageConfiguration = project.extensions.getByType(DeltaCoverageConfiguration::class.java)

        // WHEN
        config.view(customView) { view ->
            view.violationRules.failIfCoverageLessThan(expectedMinCoverage)
        }

        // THEN
        assertSoftly(config.reportViews.getByName(customView)) {
            shouldNotBeNull()
            violationRules.failOnViolation.get().shouldBeTrue()

            violationRules.rules.get().values.map { it.minCoverageRatio.get() }.shouldContainExactly(
                List(CoverageEntity.entries.size) { expectedMinCoverage }
            )
        }
    }
}
