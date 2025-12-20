package io.github.surpsg.deltacoverage.gradle

import io.github.gwkit.gradleprobe.RestorableFile
import io.github.gwkit.gradleprobe.assertion.assertOutputContainsStrings
import io.github.gwkit.gradleprobe.junit.GradlePluginTest
import io.github.gwkit.gradleprobe.junit.GradleRunnerInstance
import io.github.gwkit.gradleprobe.junit.ProjectFile
import io.github.gwkit.gradleprobe.junit.RootProjectDir
import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_COVERAGE_TASK
import io.github.surpsg.deltacoverage.gradle.task.DeltaCoverageTask
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
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
    fun `deltaCoverage should generate explain report when explain flag is set`() {
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
            .withArguments(DELTA_COVERAGE_TASK, "-P${DeltaCoverageTask.EXPLAIN_PROPERTY}")
            .build()
            .apply { println(output) }

        // THEN
        assertSoftly {
            result.task(":${DELTA_COVERAGE_TASK}Test")?.outcome shouldBe TaskOutcome.SUCCESS

            val reportFile = rootProjectDir
                .resolve("build/reports/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}")
                .resolve(DeltaCoverageTask.EXPLAIN_DIR)
                .resolve("test.md")
            reportFile.shouldExist()

            val reportContent = reportFile.readText()
            reportContent shouldContain "# Delta Coverage Explain Report: `test`"
            reportContent shouldContain "## Plugin Configuration"
            reportContent shouldContain "## Diff Configuration"
            reportContent shouldContain "## View Details"
        }
    }

    @Test
    fun `deltaCoverage should generate only explain report when explainOnly flag is set`() {
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
            .withArguments(DELTA_COVERAGE_TASK, "-P${DeltaCoverageTask.EXPLAIN_ONLY_PROPERTY}")
            .build()
            .apply { println(output) }

        // THEN
        assertSoftly {
            result.task(":${DELTA_COVERAGE_TASK}Test")?.outcome shouldBe TaskOutcome.SUCCESS

            // Explain report should exist
            val reportFile = rootProjectDir
                .resolve("build/reports/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}")
                .resolve(DeltaCoverageTask.EXPLAIN_DIR)
                .resolve("test.md")
            reportFile.shouldExist()

            // Coverage report should NOT be generated (no Delta Coverage Stats in output)
            result.output shouldNotContain "Delta Coverage Stats"
            result.output shouldContain "Delta Coverage explain report generated:"
        }
    }

    @Test
    fun `deltaCoverage should not generate explain report when explain flag is not set`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
            }
        """.trimIndent()
        )

        // Clean up explain directory from previous test runs
        val explainDir = rootProjectDir
            .resolve("build/reports/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}")
            .resolve(DeltaCoverageTask.EXPLAIN_DIR)
        explainDir.deleteRecursively()

        // WHEN
        gradleRunner
            .withArguments(DELTA_COVERAGE_TASK)
            .build()

        // THEN
        val reportFile = explainDir.resolve("test.md")
        reportFile.shouldNotExist()
    }

    @Test
    fun `explain report should include diff source configuration`() {
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
            .withArguments(DELTA_COVERAGE_TASK, "-P${DeltaCoverageTask.EXPLAIN_PROPERTY}")
            .build()

        // THEN
        val reportContent = rootProjectDir
            .resolve("build/reports/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}")
            .resolve(DeltaCoverageTask.EXPLAIN_DIR)
            .resolve("test.md")
            .readText()

        assertSoftly {
            reportContent shouldContain "- Source: file"
            reportContent shouldContain diffFilePath
        }
    }

    @Test
    fun `explain report should include view configuration`() {
        // GIVEN
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                view('test') {
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
            .withArguments(DELTA_COVERAGE_TASK, "-P${DeltaCoverageTask.EXPLAIN_PROPERTY}")
            .build()

        // THEN
        val reportFile = rootProjectDir
            .resolve("build/reports/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}")
            .resolve(DeltaCoverageTask.EXPLAIN_DIR)
            .resolve("test.md")

        assertSoftly {
            reportFile.shouldExist()

            val reportContent = reportFile.readText()
            reportContent shouldContain "# Delta Coverage Explain Report: `test`"
            reportContent shouldContain "| Origin | discovered |"
            reportContent shouldContain "`**/Custom*`"
            reportContent shouldContain "| instruction | 0.8 |"
            reportContent shouldContain "- Fail on violation: true"
        }
    }

    @Test
    fun `explain report should include exclude classes configuration`() {
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
            .withArguments(DELTA_COVERAGE_TASK, "-P${DeltaCoverageTask.EXPLAIN_PROPERTY}")
            .build()

        // THEN
        val reportContent = rootProjectDir
            .resolve("build/reports/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}")
            .resolve(DeltaCoverageTask.EXPLAIN_DIR)
            .resolve("test.md")
            .readText()

        assertSoftly {
            reportContent shouldContain "`**/generated/**`"
            reportContent shouldContain "`**/dto/**`"
        }
    }

    @Test
    fun `explain report should print report location`() {
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
            .withArguments(DELTA_COVERAGE_TASK, "-P${DeltaCoverageTask.EXPLAIN_PROPERTY}")
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
