package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.test.GradlePluginTest
import io.github.surpsg.deltacoverage.gradle.test.GradleRunnerInstance
import io.github.surpsg.deltacoverage.gradle.test.ProjectFile
import io.github.surpsg.deltacoverage.gradle.test.RestorableFile
import io.github.surpsg.deltacoverage.gradle.test.RootProjectDir
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.file.shouldBeADirectory
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.file.shouldExist
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

@GradlePluginTest(TestProjects.SINGLE_MODULE)
class DeltaCoverageReportsTest {

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
    fun `delta-coverage should create all jacoco reports`() {
        // GIVEN
        val view = "test"
        val baseReportDir = "build/custom/reports/dir/jacoco/"
        buildFile.file.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                reports {
                    baseReportDir.set("$baseReportDir")
                    html.set(true)
                    xml.set(true)
                    console.set(true)
                    markdown.set(true)
                    fullCoverageReport.set(true)
                }
                reportViews {
                    $view {
                        violationRules {
                            failIfCoverageLessThan(0.6d)
                            failOnViolation.set(false)
                        }
                    }
                    aggregated.enabled = true
                }
            }
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner
            .runDeltaCoverageTask()
            .assertOutputContainsStrings("Fail on violations: false. Found violations: 2")
            .assertOutputContainsStrings(
                "| [$view] Delta Coverage Stats                           |",
                "| Class                | Lines    | Branches | Instr.   |",
                "+----------------------+----------+----------+----------+",
                "| com.java.test.Class1 | 66.67%   | 50%      | 52.94%   |",
                "| Total                | ✔ 66.67% | ✖ 50%    | ✖ 52.94% |",
                "| Min expected         | 60%      | 60%      | 60%      |",
            )

        // AND THEN
        val baseReportDirFile = rootProjectDir.resolve(baseReportDir).resolve("coverage-reports")
        assertAllReportsCreated(baseReportDirFile.resolve("delta-coverage/$view/"))
        assertAllReportsCreated(baseReportDirFile.resolve("full-coverage-report/$view/"))

        // AND THEN
        assertSummaryReport(baseReportDirFile)
    }

    private fun assertSummaryReport(baseReportDirFile: File) {
        assertSoftly(baseReportDirFile) {
            shouldExist()
            shouldContainFile("test-summary.json")
            shouldContainFile("aggregated-summary.json")
        }
    }

    private fun assertAllReportsCreated(baseReportDir: File) {
        val htmlReportDir = baseReportDir.resolve("html")
        assertSoftly(htmlReportDir) {
            shouldExist()
            shouldBeADirectory()
            shouldContainFile("index.html")
            shouldContainFile("com.java.test")
        }
        sequenceOf("report.xml", "report.md")
            .map { file -> baseReportDir.resolve(file) }
            .forEach { reportFile ->
                assertSoftly(reportFile) {
                    shouldExist()
                    shouldBeAFile()
                }
            }
    }
}
