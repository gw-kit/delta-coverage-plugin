package io.github.surpsg.deltacoverage.gradle.task

import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration
import io.github.surpsg.deltacoverage.gradle.unittest.applyDeltaCoveragePlugin
import io.github.surpsg.deltacoverage.gradle.unittest.testJavaProject
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.file.shouldExist
import org.gradle.api.internal.project.ProjectInternal
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
}
