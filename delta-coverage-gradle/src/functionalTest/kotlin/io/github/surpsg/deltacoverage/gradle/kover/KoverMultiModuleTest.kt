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
        val testViewName = "test"
        val intTestViewName = "intTest"
        buildFile.file.appendText(
            """
            configure<DeltaCoverageConfiguration> {
                coverage.engine = CoverageEngine.INTELLIJ
                diffSource.file = "$diffFilePath"
                reports {
                    html = true
                    baseReportDir = "$baseReportDir"
                }
                reportViews {
                    val $testViewName by getting {
                        violationRules {
                            failIfCoverageLessThan(0.9)
                            failOnViolation = false
                        }
                    }
                    val $intTestViewName by getting {
                        violationRules {
                            failIfCoverageLessThan(0.6)
                            failOnViolation = false
                        }
                    }
                }
            }
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner
            .runDeltaCoverageTask(gradleArgs = arrayOf("intTest"))
            .assertOutputContainsStrings(
                "[view:$testViewName] Fail on violations: false. Found violations: 2",
                "[view:$testViewName] BRANCH: expectedMin=0.9, actual=0.5",
                "[view:$testViewName] INSTRUCTION: expectedMin=0.9, actual=0.8",

                "[view:$intTestViewName] Fail on violations: false. Found violations: 1",
                "[view:$intTestViewName] INSTRUCTION: expectedMin=0.6, actual=0.4",
            )

        // AND THEN
        assertSoftly {
            listOf(testViewName, intTestViewName).forEach { view ->
                val htmlReportDir = rootProjectDir.resolve(baseReportDir)
                    .resolve(File("coverage-reports/delta-coverage/$view/html/"))
                htmlReportDir.shouldExist()
                htmlReportDir.shouldBeADirectory()
                htmlReportDir.shouldContainFile("index.html")
            }
        }
    }
}
