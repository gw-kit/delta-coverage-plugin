package io.github.surpsg.deltacoverage.gradle

import io.github.gwkit.gradleprobe.RestorableFile
import io.github.gwkit.gradleprobe.assertion.assertOutputContainsStrings
import io.github.gwkit.gradleprobe.gradlerunner.runTask
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
        buildFile.file.appendText("""
            |deltaCoverageReport {
            |   diffSource.file.set('$diffFilePath')
            |}
        """.trimMargin())

        // WHEN
        gradleRunner.runDeltaCoverageTask(
            printLogs = true,
            gradleArgs = arrayOf("-P${DeltaCoverageTask.EXPLAIN_PROPERTY}"),
        )

        // THEN
        val reportFile = rootProjectDir
            .resolve("build/reports/${DeltaCoverageTask.BASE_COVERAGE_REPORTS_DIR}")
            .resolve("test-explain-report.md")
        assertSoftly(reportFile) {
            println("file://$reportFile")
            shouldExist()

            assertSoftly(reportFile.readText()) {
                shouldContain("# Delta Coverage Explain Report: `test`")
                shouldContain("## Plugin Configuration")
                shouldContain("## Diff Configuration")
                shouldContain("## 'test' View Details")
            }
        }
    }
}
