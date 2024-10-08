package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTask
import io.github.surpsg.deltacoverage.gradle.unittest.applyDeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.unittest.applyPlugin
import io.github.surpsg.deltacoverage.gradle.unittest.newProject
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.maps.shouldHaveKeys
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.junit.jupiter.api.Test

class DeltaCoveragePluginTest {

    @Test
    fun `apply plugin should automatically create aggregated view and views from test tasks`() {
        // GIVEN
        val parentProj: ProjectInternal = testJavaProject(attachSettings = true) {
            newProject {
                withName("child")
                withParent(this@testJavaProject)
            }
            allprojects { proj -> proj.applyPlugin<JavaPlugin>() }
            applyDeltaCoveragePlugin()
        }

        // WHEN
        parentProj.evaluate()

        // THEN
        val config = parentProj.extensions.getByType(DeltaCoverageConfiguration::class.java)
        config.reportViews.names.shouldContainExactlyInAnyOrder(TEST_VIEW, AGGREGATED_VIEW)

        // AND THEN
        val deltaCoverageTask = parentProj.tasks.withType(DeltaCoverageTask::class.java).first()
        deltaCoverageTask.sourcesFiles.get().shouldNotBeEmpty()
        deltaCoverageTask.classesFiles.get().shouldNotBeEmpty()
        assertSoftly(deltaCoverageTask.coverageBinaryFiles.get()) {
            shouldHaveKeys(AGGREGATED_VIEW, TEST_VIEW)
            get(AGGREGATED_VIEW).shouldNotBeEmpty()
            get(TEST_VIEW).shouldNotBeEmpty()
        }
    }

    private companion object {
        const val TEST_VIEW = "test"
        const val AGGREGATED_VIEW = "aggregated"
    }
}
