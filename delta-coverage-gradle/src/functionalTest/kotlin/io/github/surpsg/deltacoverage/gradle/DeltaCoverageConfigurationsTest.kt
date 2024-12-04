package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_COVERAGE_TASK
import io.github.surpsg.deltacoverage.gradle.test.GradlePluginTest
import io.github.surpsg.deltacoverage.gradle.test.GradleRunnerInstance
import io.github.surpsg.deltacoverage.gradle.test.ProjectFile
import io.github.surpsg.deltacoverage.gradle.test.RestorableFile
import org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@GradlePluginTest(TestProjects.SINGLE_MODULE)
class DeltaCoverageConfigurationsTest {

    @ProjectFile("test.diff.file")
    lateinit var diffFilePath: String

    @ProjectFile("build.gradle")
    lateinit var buildFile: RestorableFile

    @GradleRunnerInstance
    lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun beforeEach() {
        buildFile.restoreOriginContent()
    }

    @Test
    fun `delta-coverage should fail if classes file collection is empty`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                classesDirs = files()
            }
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner
            .runTaskAndFail(TEST_TASK_NAME, DELTA_COVERAGE_TASK)
            .assertOutputContainsStrings("'deltaCoverageReport.classesDirs' file collection is empty.")
    }

    @Test
    fun `delta-coverage outputs caching should be disabled`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
            }
        """.trimIndent()
        )
        gradleRunner.runDeltaCoverageTask()

        // WHEN // THEN
        gradleRunner.runDeltaCoverageTask()
    }

    @Test
    fun `should create delta coverage tasks`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
            }
        """.trimIndent()
        )

        // WHEN
        gradleRunner.runTask(DELTA_COVERAGE_TASK, "--dry-run")
            .assertOutputContainsStrings(
                "':deltaCoverage'",
                "':deltaCoverageTest'",
                "':deltaCoverageAggregated'",
            )
    }
}
