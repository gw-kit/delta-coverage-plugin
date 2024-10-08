package io.github.surpsg.deltacoverage.gradle.kover

import io.github.surpsg.deltacoverage.gradle.TestProjects
import io.github.surpsg.deltacoverage.gradle.assertOutputContainsStrings
import io.github.surpsg.deltacoverage.gradle.runDeltaCoverageTask
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

@GradlePluginTest(TestProjects.SINGLE_MODULE, kts = true)
class KoverReportsTest {

    @RootProjectDir
    lateinit var rootProjectDir: File

    @ProjectFile("test.diff.file")
    lateinit var diffFilePath: String

    @ProjectFile("build.gradle.kts")
    lateinit var buildFile: RestorableFile

    @GradleRunnerInstance
    lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun beforeEach() {
        buildFile.restoreOriginContent()
    }

    @Test
    fun `delta-coverage should create all reports`() {
        // GIVEN
        val baseReportDir = "build/custom/reports/dir/kover/"
        val view = "test"
        buildFile.file.appendText(
            """
            configure<DeltaCoverageConfiguration> {
                coverage {
                    engine = CoverageEngine.INTELLIJ
                    autoApplyPlugin = true
                }
                diffSource.file = "$diffFilePath"
                reports {
                    baseReportDir.set("$baseReportDir")
                    html = true
                    xml = true
                    console = true
                    markdown = true
                    fullCoverageReport.set(true)
                }
                reportViews {
                    val $view by getting {
                        violationRules {
                            failIfCoverageLessThan(0.7)
                            failOnViolation = false
                        }
                    }
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
                "| com.java.test.Class1 | 66.67%   | 75%      | 66.67%   |",
                "| Total                | ✖ 66.67% | ✔ 75%    | ✖ 66.67% |",
                "| Min expected         | 70%      | 70%      | 70%      |",
            )

        // AND THEN
        val baseReportDirFile = rootProjectDir.resolve(baseReportDir).resolve("coverage-reports")
        assertAllReportsCreated(baseReportDirFile.resolve("delta-coverage/$view"))
        assertAllReportsCreated(baseReportDirFile.resolve("full-coverage-report/$view"))
    }

    private fun assertAllReportsCreated(baseReportDir: File) {
        val htmlReportDir = baseReportDir.resolve("html")
        assertSoftly(htmlReportDir) {
            shouldExist()
            shouldBeADirectory()
            shouldContainFile("index.html")
        }
        val xmlReportFile = baseReportDir.resolve("report.xml")
        assertSoftly(xmlReportFile) {
            shouldExist()
            shouldBeAFile()
        }

        val markdownReportFile = baseReportDir.resolve("report.md")
        assertSoftly(markdownReportFile) {
            shouldExist()
            shouldBeAFile()
        }
    }
}
