package io.github.surpsg.deltacoverage.gradle

import io.github.surpsg.deltacoverage.gradle.DeltaCoveragePlugin.Companion.DELTA_COVERAGE_TASK
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

class DeltaCoverageReportsTest : BaseDeltaCoverageTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "single-module-test-project"
    }

    override fun buildTestConfiguration() = TestConfiguration(
        TEST_PROJECT_RESOURCE_NAME,
        "build.gradle",
        "test.diff.file"
    )

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "html,  html,       true",
            "csv,   report.csv, false",
            "xml,   report.xml, false"
        ]
    )
    fun `delta-coverage should create single report type`(
        reportToGenerate: String,
        expectedReportFile: String,
        isDirectory: Boolean
    ) {
        // setup
        val baseReportDir = "build/custom/reports/dir/jacoco/"
        buildFile.appendText(
            """
            deltaCoverageReport {
                diffSource.file.set('$diffFilePath')
                reportConfiguration.baseReportDir.set('$baseReportDir')
                reportConfiguration.$reportToGenerate.set(true)
            }
        """.trimIndent()
        )

        // run
        val result = gradleRunner.runTask(DELTA_COVERAGE_TASK)

        // assert
        result.assertDeltaCoverageStatusEqualsTo(SUCCESS)
            .assertOutputContainsStrings("Fail on violations: false. Found violations: 0")

        val diffReportDir: File = rootProjectDir.resolve(baseReportDir).resolve("deltaCoverage")
        assertThat(diffReportDir.list()!!.toList())
            .hasSize(1).first()
            .extracting(
                { it },
                { diffReportDir.resolve(it).isDirectory }
            )
            .containsExactly(expectedReportFile, isDirectory)
    }

    @Test
    fun `delta-coverage should create deltaCoverage dir and full coverage with html, csv and xml reports`() {
        // setup
        val baseReportDir = "build/custom/reports/dir/jacoco/"
        buildFile.appendText(
            """
            
            deltaCoverageReport {
                diffSource {
                    file.set('$diffFilePath')
                }
                jacocoExecFiles = jacocoTestReport.executionData
                classesDirs = jacocoTestReport.classDirectories
                srcDirs = jacocoTestReport.sourceDirectories
                
                reports {
                    html.set(true)
                    xml.set(true)
                    csv.set(true)
                    fullCoverageReport.set(true)
                    baseReportDir.set('$baseReportDir')
                }
            }
        """.trimIndent()
        )

        // run
        val result = gradleRunner.runTask(DELTA_COVERAGE_TASK)

        // assert
        result.assertDeltaCoverageStatusEqualsTo(SUCCESS)
        rootProjectDir.resolve(baseReportDir).apply {
            assertAllReportsCreated(resolve("deltaCoverage"))
            assertAllReportsCreated(resolve("fullReport"))
        }
    }

    private fun assertAllReportsCreated(baseReportDir: File) {
        assertThat(baseReportDir.list()).containsExactlyInAnyOrder("report.xml", "report.csv", "html")
        assertThat(baseReportDir.resolve("html").list())
            .containsExactlyInAnyOrder(
                *expectedHtmlReportFiles("com.java.test")
            )
    }
}
