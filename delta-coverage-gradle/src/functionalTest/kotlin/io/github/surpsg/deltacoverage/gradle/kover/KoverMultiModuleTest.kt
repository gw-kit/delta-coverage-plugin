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
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.file.shouldExist
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

@GradlePluginTest(resourceProjectDir = TestProjects.MULTI_MODULE, kts = true)
class KoverMultiModuleTest {

    @RootProjectDir
    lateinit var rootProjectDir: File

    @ProjectFile("test.diff")
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
    fun `should build delta coverage report from multi module project `() {
        // GIVEN
        val baseReportDir = "build/custom/kover/"
        buildFile.file.appendText(
            """
            configure<DeltaCoverageConfiguration> {
                coverage.engine = CoverageEngine.INTELLIJ
                diffSource.file = "$diffFilePath"
                reports {
                    html.set(true)
                    baseReportDir.set("$baseReportDir")
                }
                reportViews {
                    val default by getting {
                        violationRules {
                            failIfCoverageLessThan(0.9)
                            failOnViolation.set(false)
                        }
                    }
                    create("intTests") {
                        violationRules {
                            failIfCoverageLessThan(0.7)
                            failOnViolation.set(false)
                        }
                    }
                }
            }
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner
            .runDeltaCoverageTask()
            .assertOutputContainsStrings(
                "[view:default] Fail on violations: false. Found violations: 2",
                "[view:default] BRANCH: expectedMin=0.9, actual=0.5",
                "[view:default] INSTRUCTION: expectedMin=0.9, actual=0.8",

                "[view:intTests] Fail on violations: false. Found violations: 1",
                "[view:intTests] BRANCH: expectedMin=0.7, actual=0.5",
            )

        // AND THEN
        assertSoftly {
            listOf("default", "intTests").forEach { view ->
                val htmlReportDir = rootProjectDir.resolve(baseReportDir)
                    .resolve(File("coverage-reports/delta-coverage/$view/html/"))
                htmlReportDir.shouldExist()
                htmlReportDir.shouldBeADirectory()
                htmlReportDir.shouldContainFile("index.html")
            }
        }
    }
}
