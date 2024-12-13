package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTask
import io.github.surpsg.deltacoverage.gradle.task.NativeGitDiffTask
import io.github.surpsg.deltacoverage.gradle.unittest.applyDeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.unittest.applyPlugin
import io.github.surpsg.deltacoverage.gradle.unittest.newProject
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider
import org.junit.jupiter.api.Test

class DeltaCoveragePluginTest {

    @Test
    fun `apply plugin should automatically create aggregated view and views from test tasks`() {
        // GIVEN
        val customViewName = "custom"
        val parentProj: ProjectInternal = testJavaProject(attachSettings = true) {
            newProject {
                withName("child")
                withParent(this@testJavaProject)
            }
            allprojects { proj -> proj.applyPlugin<JavaPlugin>() }
            applyDeltaCoveragePlugin()
            extensions.configure(DeltaCoverageConfiguration::class.java) { config ->
                config.reportViews.register(customViewName) {
                    it.coverageBinaryFiles = files("1.txt")
                }
            }
        }

        // WHEN
        val config = parentProj.extensions.getByType(DeltaCoverageConfiguration::class.java)

        // THEN
        config.reportViews.names.shouldContainExactlyInAnyOrder(TEST_VIEW, AGGREGATED_VIEW, customViewName)

        // AND THEN
        val deltaCoverageTasks = parentProj.tasks.withType(DeltaCoverageTask::class.java)
        assertSoftly(deltaCoverageTasks) {
            forEach { task ->
                task.sourcesFiles.get().shouldNotBeEmpty()
                task.classesFiles.get().shouldNotBeEmpty()

                val viewName: String = task.viewName.get()
                task.coverageBinaryFiles.get()[viewName].shouldNotBeEmpty()
            }
        }

        // AND THEN
        deltaCoverageTasks.names shouldContainExactlyInAnyOrder listOf(
            "deltaCoverageTest",
            "deltaCoverageCustom",
            "deltaCoverageAggregated",
        )
    }

    @Test
    fun `apply plugin should create native git task`() {
        // GIVEN
        val proj: ProjectInternal = testJavaProject(attachSettings = true) {
            applyDeltaCoveragePlugin()
            extensions.configure(DeltaCoverageConfiguration::class.java) { config ->
                config.diffSource.git.useNativeGit.set(true)
                config.reportViews.register("custom")
            }
        }

        // WHEN
        val gitDiffTask = proj.tasks.findByName(DeltaCoveragePlugin.GIT_DIFF_TASK)

        // THEN
        gitDiffTask.shouldBeInstanceOf<NativeGitDiffTask>()

        // AND THEN
        val deltaCoverageTasks = proj.tasks.withType(DeltaCoverageTask::class.java)
        deltaCoverageTasks
            .forEach { deltaTask ->
                val gitDiffTask = deltaTask.dependsOn.firstNotNullOfOrNull {
                    it as? TaskProvider<NativeGitDiffTask>
                }
                gitDiffTask.shouldNotBeNull()
            }
    }

    private companion object {
        const val TEST_VIEW = "test"
        const val AGGREGATED_VIEW = "aggregated"
    }
}
