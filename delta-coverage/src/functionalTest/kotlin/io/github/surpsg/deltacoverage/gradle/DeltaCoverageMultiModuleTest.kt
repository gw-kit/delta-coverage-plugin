package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_COVERAGE_TASK
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DeltaCoverageMultiModuleTest : BaseDeltaCoverageTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "multi-module-test-project"
    }

    override fun buildTestConfiguration() = TestConfiguration(
        TEST_PROJECT_RESOURCE_NAME,
        "build.gradle",
        "test.diff"
    )

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @Test
    fun `delta-coverage should automatically collect jacoco configuration from submodules in multimodule project`() {
        // setup
        val baseReportDir = "build/custom/"
        val htmlReportDir = rootProjectDir.resolve(baseReportDir).resolve(File("deltaCoverage", "html"))
        buildFile.appendText(
            """
            
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                reports {
                    html.set(true)
                    baseReportDir.set('$baseReportDir')
                }
                violationRules.failIfCoverageLessThan 0.9
            }
        """.trimIndent()
        )

        // run
        val result = gradleRunner.runTaskAndFail(DELTA_COVERAGE_TASK)

        // assert
        result.assertDeltaCoverageStatusEqualsTo(FAILED)
            .assertOutputContainsStrings(
                "Fail on violations: true. Found violations: 1.",
                "Rule violated for bundle $TEST_PROJECT_RESOURCE_NAME: " +
                        "branches covered ratio is 0.5, but expected minimum is 0.9"
            )
        assertThat(htmlReportDir.list()).containsExactlyInAnyOrder(
            *expectedHtmlReportFiles("com.module1", "com.module2")
        )
    }

    @Test
    fun `delta-coverage plugin should auto-apply jacoco to project and subprojects`() {
        // setup
        val expectedCoverageRatio = 0.8
        buildFile.writeText(rootBuildScriptWithoutJacocoPlugin(expectedCoverageRatio))

        // run // assert
        gradleRunner.runTaskAndFail("test", DELTA_COVERAGE_TASK)
            .assertDeltaCoverageStatusEqualsTo(FAILED)
            .assertOutputContainsStrings(
                "Fail on violations: true. Found violations: 1.",
                "Rule violated for bundle $TEST_PROJECT_RESOURCE_NAME: " +
                        "branches covered ratio is 0.5, but expected minimum is $expectedCoverageRatio"
            )
    }

    @Test
    fun `delta-coverage plugin should not apply jacoco plugin if jacoco auto-apply is disabled`() {
        // setup
        buildFile.writeText(rootBuildScriptWithoutJacocoPlugin(1.0))

        // disable jacoco auto-apply
        rootProjectDir.resolve("gradle.properties").appendText("""
            io.github.surpsg.delta-coverage.auto-apply-jacoco=false
        """.trimIndent())

        // manually apply jacoco only to 'module1'
        rootProjectDir.resolve("module1").resolve("build.gradle").appendText("""

            apply plugin: 'jacoco'
        """.trimIndent())

        // run // assert
        gradleRunner
            .runTask("test", DELTA_COVERAGE_TASK)
            .assertDeltaCoverageStatusEqualsTo(SUCCESS)
    }

    private fun rootBuildScriptWithoutJacocoPlugin(expectedCoverageRatio: Double) = """
        plugins {
            id 'java'
            id 'io.github.surpsg.delta-coverage'
        }
        repositories {
            mavenCentral()
        }
        subprojects {
            apply plugin: 'java'
            repositories {
                mavenCentral()
            }
            dependencies {
                testImplementation 'junit:junit:4.13.2'
            }
        }
        deltaCoverageReport {
            diffSource.file.set('$diffFilePath')
            violationRules.failIfCoverageLessThan $expectedCoverageRatio
        }
    """.trimIndent()

}
