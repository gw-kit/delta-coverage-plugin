package io.github.surpsg.deltacoverage.gradle

import io.github.gwkit.gradleprobe.RestorableFile
import io.github.gwkit.gradleprobe.assertion.assertOutputContainsStrings
import io.github.gwkit.gradleprobe.gradlerunner.runTask
import io.github.gwkit.gradleprobe.junit.GradlePluginTest
import io.github.gwkit.gradleprobe.junit.GradleRunnerInstance
import io.github.gwkit.gradleprobe.junit.ProjectFile
import io.github.gwkit.gradleprobe.junit.RootProjectDir
import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_EXPLAIN_TASK
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTask
import io.github.surpsg.deltacoverage.gradle.task.DeltaExplainTask
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

@GradlePluginTest(TestProjects.SINGLE_MODULE, kts = false)
class DeltaExplainTaskTest {

    @RootProjectDir
    lateinit var rootProjectDir: File

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
    fun `deltaExplain task should generate explain report`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
            }
        """.trimIndent()
        )

        // WHEN
        val result = gradleRunner
            .withArguments(DELTA_EXPLAIN_TASK)
            .build()
            .apply {
                println(output)
            }

        // THEN
        assertSoftly {
            result.task(":$DELTA_EXPLAIN_TASK")?.outcome shouldBe TaskOutcome.SUCCESS

            val reportDir = rootProjectDir.resolve("build/reports/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}")
            reportDir.shouldExist()
            reportDir.shouldContainFile(DeltaExplainTask.EXPLAIN_REPORT_FILE_NAME)

            val reportContent = reportDir.resolve(DeltaExplainTask.EXPLAIN_REPORT_FILE_NAME).readText()
            reportContent shouldContain "# Delta Coverage Explain Report"
            reportContent shouldContain "## Plugin Configuration"
            reportContent shouldContain "## Diff Configuration"
            reportContent shouldContain "## Views"
            reportContent shouldContain "### View: `test`"
        }
    }

    @Test
    fun `deltaExplain task should include diff source configuration`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
            }
        """.trimIndent()
        )

        // WHEN
        gradleRunner
            .withArguments(DELTA_EXPLAIN_TASK)
            .build()

        // THEN
        val reportContent = rootProjectDir
            .resolve("build/reports/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}")
            .resolve(DeltaExplainTask.EXPLAIN_REPORT_FILE_NAME)
            .readText()

        assertSoftly {
            reportContent shouldContain "- Source: file"
            reportContent shouldContain diffFilePath
        }
    }

    @Test
    fun `deltaExplain task should include custom view configuration`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                view('customView') {
                    enabled.set(true)
                    matchClasses.set(['**/Custom*'])
                    violationRules {
                        failOnViolation.set(true)
                        failIfCoverageLessThan(0.8d)
                    }
                }
            }
        """.trimIndent()
        )

        // WHEN
        gradleRunner
            .withArguments(DELTA_EXPLAIN_TASK)
            .build()

        // THEN
        val reportContent = rootProjectDir
            .resolve("build/reports/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}")
            .resolve(DeltaExplainTask.EXPLAIN_REPORT_FILE_NAME)
            .readText()

        assertSoftly {
            reportContent shouldContain "### View: `customView`"
            reportContent shouldContain "| Origin | manual |"
            reportContent shouldContain "`**/Custom*`"
            reportContent shouldContain "| instruction | 0.8 |"
            reportContent shouldContain "- Fail on violation: true"
        }
    }

    @Test
    fun `deltaExplain task should include exclude classes configuration`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                excludeClasses.value(['**/generated/**', '**/dto/**'])
            }
        """.trimIndent()
        )

        // WHEN
        gradleRunner
            .withArguments(DELTA_EXPLAIN_TASK)
            .build()

        // THEN
        val reportContent = rootProjectDir
            .resolve("build/reports/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}")
            .resolve(DeltaExplainTask.EXPLAIN_REPORT_FILE_NAME)
            .readText()

        assertSoftly {
            reportContent shouldContain "`**/generated/**`"
            reportContent shouldContain "`**/dto/**`"
        }
    }

    @Test
    fun `deltaExplain task should print report location`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
            }
        """.trimIndent()
        )

        // WHEN
        val result = gradleRunner
            .withArguments(DELTA_EXPLAIN_TASK)
            .build()

        // THEN
        result.assertOutputContainsStrings("Delta Coverage explain report generated:")
    }

    private infix fun TaskOutcome?.shouldBe(expected: TaskOutcome) {
        if (this != expected) {
            throw AssertionError("Expected task outcome $expected but was $this")
        }
    }
}
