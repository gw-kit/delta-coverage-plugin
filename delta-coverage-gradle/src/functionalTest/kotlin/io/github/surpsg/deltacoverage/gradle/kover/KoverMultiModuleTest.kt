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
import org.gradle.api.plugins.JavaPlugin
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
                    html = true
                    baseReportDir = "$baseReportDir"
                }
                reportViews {
                    val $TEST_TASK by getting {
                        violationRules.failIfCoverageLessThan(0.9)
                        violationRules.failOnViolation = false
                    }
                    val $INT_TEST_TASK by getting {
                        violationRules.failIfCoverageLessThan(0.6)
                        violationRules.failOnViolation = false
                    }
                    val $AGG_VIEW by getting {
                        violationRules.failIfCoverageLessThan(1.0)
                        violationRules.failOnViolation = false
                    }
                }
            }
        """.trimIndent()
        )

        // WHEN // THEN
        gradleRunner
            .runDeltaCoverageTask(gradleArgs = arrayOf(INT_TEST_TASK))
            .assertOutputContainsStrings(
                "[$TEST_TASK] Fail on violations: false. Found violations: 2.",
                "BRANCH: expectedMin=0.9, actual=0.5",
                "INSTRUCTION: expectedMin=0.9, actual=0.88",

                "[$INT_TEST_TASK] Fail on violations: false. Found violations: 3.",
                "INSTRUCTION: expectedMin=0.6, actual=0.16",
                "BRANCH: expectedMin=0.6, actual=0.25",
                "LINE: expectedMin=0.6, actual=0.2",

                "[$AGG_VIEW] Fail on violations: false. Found violations: 2.",
                "INSTRUCTION: expectedMin=1.0, actual=0.8",
                "BRANCH: expectedMin=1.0, actual=0.75",
            )

        // AND THEN
        assertSoftly {
            listOf(
                TEST_TASK,
                INT_TEST_TASK,
                AGG_VIEW,
            ).forEach { view ->
                val htmlReportDir = rootProjectDir.resolve(baseReportDir)
                    .resolve(File("coverage-reports/delta-coverage/$view/html/"))
                htmlReportDir.shouldExist()
                htmlReportDir.shouldBeADirectory()
                htmlReportDir.shouldContainFile("index.html")
            }
        }
    }

    private companion object {
        const val TEST_TASK = JavaPlugin.TEST_TASK_NAME
        const val INT_TEST_TASK = "intTest"
        const val AGG_VIEW = "aggregated"
    }
}
