package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.unittest.applyDeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DeltaCoverageTaskTest {

    @Suppress("VarCouldBeVal")
    @TempDir
    private lateinit var tempDir: File

    @Test
    fun `should create summary report for all views`() {
        // GIVEN
        val customView = "someCustom"
        val project: ProjectInternal = testJavaProject {
            applyDeltaCoveragePlugin()

            val diffFile = tempDir.resolve("111").apply {
                createNewFile()
            }

            extensions.configure(DeltaCoverageConfiguration::class.java) { config ->
                with(config) {
                    diffSource { source ->
                        source.file.set(diffFile.absolutePath)
                    }
                    reportViews.getByName("test").coverageBinaryFiles = files("any")
                    view(customView) {
                        it.coverageBinaryFiles = files("any-custom")
                    }
                }
            }
        }

        // WHEN
        project.tasks.withType(DeltaCoverageTask::class.java).forEach { task ->
            task.executeAction()
        }

        // THEN
        assertSoftly {
            listOf(
                "test-summary.json",
                "aggregated-summary.json",
                "$customView-summary.json",
            ).forEach { summaryFileName ->
                val summaryFile = "reports/coverage-reports/$summaryFileName"
                val actualSummaryFile = project.layout.buildDirectory.file(summaryFile).get().asFile
                actualSummaryFile.shouldExist()
            }
        }
    }

    @Test
    fun `delta coverage task should depend on classes`() {
        // GIVEN
        val project: ProjectInternal = testJavaProject {
            applyDeltaCoveragePlugin()

            val diffFile = tempDir.resolve("111").apply {
                createNewFile()
            }

            extensions.configure(DeltaCoverageConfiguration::class.java) { config ->
                with(config) {
                    diffSource { source ->
                        source.file.set(diffFile.absolutePath)
                    }
                }
            }
        }

        // WHEN
        val deltaCoverageTasks = project.tasks.withType(DeltaCoverageTask::class.java).toList()

        // THEN
        println(deltaCoverageTasks)
        val dependsOnTasks: List<String?> = deltaCoverageTasks
            .flatMap { it.dependsOn }
            .filterIsInstance<Provider<out Task>>()
            .map { it.get().name }

        dependsOnTasks shouldContain JavaPlugin.CLASSES_TASK_NAME
    }

    @Nested
    inner class ExplainReportTest {

        @Test
        fun `should generate explain report when explainEnabled is true`() {
            // GIVEN
            val project: ProjectInternal = testJavaProject {
                applyDeltaCoveragePlugin()

                val diffFile = tempDir.resolve("diff.patch").apply { createNewFile() }

                extensions.configure(DeltaCoverageConfiguration::class.java) { config ->
                    config.diffSource.file.set(diffFile.absolutePath)
                    config.reportViews.getByName("test").coverageBinaryFiles = files("any")
                }
            }

            val testTask = project.tasks.withType(DeltaCoverageTask::class.java)
                .first { it.viewName.get() == "test" }
            testTask.explainEnabled.set(true)

            // WHEN
            testTask.executeAction()

            // THEN
            val explainReportFile = project.layout.buildDirectory
                .file("reports/coverage-reports/test-explain-report.md")
                .get().asFile

            explainReportFile.shouldExist()
        }

        @Test
        fun `should generate explain report when explainOnlyEnabled is true`() {
            // GIVEN
            val project: ProjectInternal = testJavaProject {
                applyDeltaCoveragePlugin()

                val diffFile = tempDir.resolve("diff.patch").apply { createNewFile() }

                extensions.configure(DeltaCoverageConfiguration::class.java) { config ->
                    config.diffSource.file.set(diffFile.absolutePath)
                    config.reportViews.getByName("test").coverageBinaryFiles = files("any")
                }
            }

            val testTask = project.tasks.withType(DeltaCoverageTask::class.java)
                .first { it.viewName.get() == "test" }
            testTask.explainOnlyEnabled.set(true)
            testTask.getOutputDir().mkdirs()

            // WHEN
            testTask.executeAction()

            // THEN
            val explainReportFile = project.layout.buildDirectory
                .file("reports/coverage-reports/test-explain-report.md")
                .get().asFile

            explainReportFile.shouldExist()

            val summaryFile = project.layout.buildDirectory
                .file("reports/coverage-reports/test-summary.json")
                .get().asFile
            summaryFile.shouldNotExist()
        }
    }
}
