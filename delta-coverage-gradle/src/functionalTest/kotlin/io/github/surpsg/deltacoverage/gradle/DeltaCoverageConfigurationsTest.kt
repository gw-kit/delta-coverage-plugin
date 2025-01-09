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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

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

    @ParameterizedTest
    @ValueSource(
        strings = [
            "classesDirs",
            "sources",
        ]
    )
    fun `delta-coverage should fail if sources file collection is empty`(
        sourceName: String
    ) {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                $sourceName = files()
            }
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner
            .runTaskAndFail(TEST_TASK_NAME, DELTA_COVERAGE_TASK)
            .assertOutputContainsStrings("'deltaCoverageReport.${sourceName}' file collection is empty.")
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
    fun `delta-coverage should successfully pass if no matched classes by include filter in changed files list`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                view('testView') {
                    coverageBinaryFiles = files('coverage.any.file')
                    violationRules.failIfCoverageLessThan(1.0d)
                    matchClasses.set([
                        '**/UnchagedClass.*',
                    ])          
                }
            }
        """.trimIndent()
        )

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
                view('myCustom') {
                    coverageBinaryFiles = files('coverage.bin')
                }
            }
        """.trimIndent()
        )

        // WHEN
        gradleRunner.runTask(DELTA_COVERAGE_TASK, "--dry-run")
            .assertOutputContainsStrings(
                "':deltaCoverage'",
                "':deltaCoverageTest'",
                "':deltaCoverageAggregated'",
                "':deltaCoverageMyCustom'",
            )
    }
}
